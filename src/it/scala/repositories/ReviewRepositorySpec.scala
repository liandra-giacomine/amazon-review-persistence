package amazonreviewpersistance

import cats.effect.IO
import io.circe.Json
import models.ReviewDocument
import munit.CatsEffectSuite
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import repositories.ReviewRepository

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class ReviewRepositorySpec extends CatsEffectSuite {

  val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newScheduledThreadPool(1))

  implicit val runtime = cats.effect.unsafe.IORuntime.global

  val reviewRepository = new ReviewRepository()(ec)

  test(
    "Collection has count ot 0 after running cleanCollection function"
  ) {
    reviewRepository.cleanCollection().unsafeRunSync()

    assertIO(
      IO.fromFuture(IO(reviewRepository.collection.countDocuments.toFuture())),
      0: Long
    )
  }

  test(
    "Collection successfully adds a list of review documents to the collection"
  ) {
    val reviewDocument1 = ReviewDocument(
      new ObjectId(),
      "abc123",
      List(1),
      1.0,
      "b",
      "c",
      "d",
      "e",
      123456789
    )

    val reviewDocument2 = ReviewDocument(
      new ObjectId(),
      "efg567",
      List(1),
      2.0,
      "f",
      "g",
      "h",
      "k",
      987654321
    )

    reviewRepository
      .insertReview(List(reviewDocument1, reviewDocument2))
      .unsafeRunSync()

    assertIO(
      IO.fromFuture(IO(reviewRepository.collection.countDocuments.toFuture())),
      2: Long
    )

    assertIO(
      IO.fromFuture(
        IO(
          reviewRepository.collection
            .find(Filters.eq("asin", reviewDocument1.asin))
            .first()
            .toFuture()
        )
      ),
      reviewDocument1
    )

    assertIO(
      IO.fromFuture(
        IO(
          reviewRepository.collection
            .find(Filters.eq("asin", reviewDocument2.asin))
            .first()
            .toFuture()
        )
      ),
      reviewDocument2
    )
  }

  test(
    "Collection groups reviews by asin and combines their rating for a given start and end timestamp"
  ) {
    val document = reviewRepository
      .getGroupedReviewRatings(123456789, 200000000)
      .unsafeRunSync()

    document.length mustBe 1
    document.head.toJson().filterNot(_.isWhitespace) mustBe Json
      .obj(
        ("_id", Json.fromString("abc123")),
        ("overallList", Json.arr(Json.fromDoubleOrString(1.0)))
      )
      .toString()
      .filterNot(_.isWhitespace)
  }

}
