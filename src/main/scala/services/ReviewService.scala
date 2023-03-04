package services

import cats.effect.IO
import fs2.io.file.{Files, Path}
import fs2.{Pipe, Stream, text}
import io.circe.parser._
import models.requests.BestReviewRequest
import models.responses.ReviewRating
import models.{ProductRatings, Review, ReviewDocument}
import org.mongodb.scala.bson.collection.immutable.Document

import scala.annotation.tailrec

class ReviewService(reviewRepository: ReviewRepository) {

  implicit val runtime = cats.effect.unsafe.IORuntime.global

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
      remainingBytes: Long,
      fs2Path: Path,
      chunkSize: Int = 1000 * 1024
  ): Unit = {
    if (remainingBytes <= 0) ()
    else {

      val end    = start + chunkSize
      val source = Files[IO].readRange(fs2Path, chunkSize, start, end)

      source
        .through(convertToReviewObjPipe)
        .through(combineReviewInListPipe)
        .through(writeToDatabaseSink)
        .compile
        .drain
        .unsafeRunSync()

      val remaining = remainingBytes - chunkSize

      insertReviewsFromFileRange(end, remaining, fs2Path)
    }
  }

  def insertReviewsFromFile(filepath: String) = {
    val path    = java.nio.file.Paths.get(filepath)
    val fs2Path = Path.fromNioPath(path)
    val size    = java.nio.file.Files.size(path)
    insertReviewsFromFileRange(0, size, fs2Path)
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

}
