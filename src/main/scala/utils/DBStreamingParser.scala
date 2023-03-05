package utils

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import fs2.io.file.{Files, Path}
import fs2.{Pipe, Stream, text}
import io.circe.parser._
import models.{Review, ReviewDocument}
import repositories.ReviewRepository

import scala.annotation.tailrec

class DBStreamingParser(reviewRepository: ReviewRepository)(implicit
    runtime: IORuntime
) {

  private val convertToReviewObjPipe: Pipe[IO, Byte, ReviewDocument] = src =>
    src
      .through(text.utf8.decode)
      .through(text.lines)
      .evalMap(line => IO(parse(line)))
      .filter(jsonEither => jsonEither.isRight)
      .evalMap(json => IO(json.toOption.get.as[Review].toOption))
      .filter(reviewOpt => reviewOpt.isDefined)
      .evalMap(reviewOpt => IO(ReviewDocument(reviewOpt.get)))

  private val combineReviewInListPipe
      : Pipe[IO, ReviewDocument, List[ReviewDocument]] = src =>
    Stream.eval(
      src.compile.toList
    )

  private val writeToDatabaseSink: Pipe[IO, List[ReviewDocument], Unit] = src =>
    src
      .evalMap { reviewList =>
        reviewRepository.insertReview(reviewList)
      }

  @tailrec
  private def insertReviewsFromFileRange(
      start: Long,
      end: Long,
      fs2Path: Path,
      fileSize: Long,
      chunkSize: Int = 1024 * 1000
  ): Unit = {
    def findNewLineByte(fromByte: Long) = {
      Files[IO]
        .readRange(fs2Path, chunkSize, fromByte, fromByte + chunkSize)
        .takeThrough(x => x != '\n' && x != '\r')
        .compile
        .count
        .map(c => c + fromByte)
    }

    if (start >= fileSize) ()
    else {
      val newEnd =
        if (end >= fileSize) fileSize
        else findNewLineByte(end + chunkSize).unsafeRunSync()

      Files[IO]
        .readRange(fs2Path, chunkSize, start, newEnd)
        .through(convertToReviewObjPipe)
        .through(combineReviewInListPipe)
        .through(writeToDatabaseSink)
        .compile
        .drain
        .unsafeRunSync()

      insertReviewsFromFileRange(
        newEnd,
        newEnd + chunkSize,
        fs2Path,
        fileSize
      )
    }
  }

  def fileToDatabaseStream(
      filepath: String
  ): IO[Either[Throwable, Unit]] = {
    IO {
      val path      = java.nio.file.Paths.get(filepath)
      val fs2Path   = Path.fromNioPath(path)
      val size      = java.nio.file.Files.size(path)
      val chunkSize = 1024 * 1000
      insertReviewsFromFileRange(0, chunkSize, fs2Path, size, chunkSize)
    }.attempt
  }

}
