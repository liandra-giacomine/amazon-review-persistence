package amazonreviewpersistance

import cats.data.EitherT
import fs2.io.file.{Files, Path}
import fs2.{Pipe, Stream, text}
import cats.effect.IO
import io.circe.parser._
import models.{ProductRatings, Review, ReviewDocument, ReviewRating}
import org.mongodb.scala.bson.collection.immutable.Document

import scala.annotation.tailrec

object ReviewService {

  implicit val runtime = cats.effect.unsafe.IORuntime.global

  private lazy val fs2Path = Path.fromNioPath(ReviewFile.path)

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
        PersistenceService.insertReview(reviewList)
      }

  @tailrec
  private def insertReviewsFromFileRange(
      start: Long,
      remainingBytes: Long,
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
      // TODO: handle error

      val remaining = remainingBytes - chunkSize

      insertReviewsFromFileRange(end, remaining)
    }
  }

  def insertReviewsFromFile() = {
    IO(insertReviewsFromFileRange(0, ReviewFile.size))
  }

  def getBestReviews(
      fromTimeStamp: Long,
      toTimeStamp: Long,
      minReviews: Int,
      limit: Int
  ): IO[Seq[ReviewRating]] = {
    for {
      reviews <- PersistenceService
        .getBestReviews(
          fromTimeStamp,
          toTimeStamp,
          minReviews,
          limit
        )
      reviewRatings <- convertDocumentToReviewRating(reviews, minReviews, limit)
    } yield reviewRatings
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
