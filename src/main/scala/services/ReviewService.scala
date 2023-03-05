package services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.jawn
import models.ReviewRatings
import models.requests.BestReviewRequest
import models.responses.ProductAverageRating
import org.mongodb.scala.Document
import repositories.ReviewRepository

class ReviewService(reviewRepository: ReviewRepository)(implicit
    val runtime: IORuntime
) {

  private def deriveProductAverageRating(
      reviewRatings: ReviewRatings
  ): ProductAverageRating = {
    val bigDecimalList = reviewRatings.overallList.map(o => BigDecimal(o))
    val averageRating  = bigDecimalList.sum / bigDecimalList.length
    ProductAverageRating(
      reviewRatings._id,
      averageRating
    )
  }

  private def calculateBestAverageRatings(
      documents: Vector[Document],
      limit: Int
  ): IO[Vector[ProductAverageRating]] = {
    IO {
      documents
        .map(d => jawn.decode[ReviewRatings](d.toJson()).toOption.get)
        .map(deriveProductAverageRating)
        .sortBy(r => r.averageRating)(Ordering.BigDecimal.reverse)
        .take(limit)
    }
  }

  def findBestReviews(
      bestReviewRequest: BestReviewRequest
  ): Either[Throwable, Vector[ProductAverageRating]] = {
    (for {
      asinAndOverallList <- reviewRepository
        .getGroupedReviewRatings(
          bestReviewRequest.start,
          bestReviewRequest.end,
          bestReviewRequest.min
        )
      productRatings <- calculateBestAverageRatings(
        asinAndOverallList,
        bestReviewRequest.limit
      )
    } yield productRatings).attempt.unsafeRunSync()
  }

}
