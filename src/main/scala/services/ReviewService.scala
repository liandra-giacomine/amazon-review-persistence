package services

import cats.effect.IO
import fs2.io.file.{Files, Path}
import fs2.{Pipe, Stream, text}
import io.circe.parser._
import models.requests.BestReviewRequest
import models.responses.ReviewRating
import models.{ProductRatings, Review, ReviewDocument}
import org.mongodb.scala.bson.collection.immutable.Document

import java.nio.charset.Charset
import scala.annotation.tailrec

class ReviewService(reviewRepository: ReviewRepository) {

  implicit val runtime = cats.effect.unsafe.IORuntime.global

  private val utf8Charset = Charset.forName("UTF-8")

  private val line = System.getProperty("line.separator")

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
      chunkSize: Int = 1024 * 2
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
      val newEnd = findNewLineByte(end + chunkSize).unsafeRunSync()

      val source =
        Files[IO].readRange(fs2Path, chunkSize, start, newEnd)

      source
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

  private def convertDocumentToReviewRating(
      documents: Seq[Document],
      minReviews: Int,
      limit: Int
  ): IO[Seq[ReviewRating]] = {
    def sumAsBigDecimal(overallList: List[Double]): BigDecimal = {
      val bigDecimalList = overallList.map(o => BigDecimal(o))
      bigDecimalList.sum / bigDecimalList.length
    }

    IO {
      documents
        .map(d => decode[ProductRatings](d.toJson()))
        .filter(p => p.toOption.get.overallList.length >= minReviews)
        .map { case Right(p) =>
          ReviewRating(
            p._id,
            sumAsBigDecimal(p.overallList)
          )
        }
        .sortBy(r => r.averageRating)(Ordering.BigDecimal.reverse)
        .take(limit)
    }
  }

  def insertReviewsFromFile(filepath: String): IO[Either[Throwable, Unit]] = {
    val path      = java.nio.file.Paths.get(filepath)
    val fs2Path   = Path.fromNioPath(path)
    val size      = java.nio.file.Files.size(path)
    val chunkSize = 1024 * 64
    IO(
      insertReviewsFromFileRange(0, chunkSize, fs2Path, size, chunkSize)
    ).attempt
  }

  def findBestReviews(
      bestReviewRequest: BestReviewRequest
  ): Either[Throwable, Seq[ReviewRating]] = {
    (for {
      reviews <- reviewRepository
        .getBestReviews(
          bestReviewRequest.start,
          bestReviewRequest.end
        )
      reviewRatings <- convertDocumentToReviewRating(
        reviews,
        bestReviewRequest.min,
        bestReviewRequest.limit
      )
    } yield reviewRatings).attempt.unsafeRunSync()
  }

}
