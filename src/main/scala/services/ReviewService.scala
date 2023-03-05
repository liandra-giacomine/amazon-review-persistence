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

  private def averageAsBigDecimal(overallList: List[Double]): BigDecimal = {
    val bigDecimalList = overallList.map(o => BigDecimal(o))
    bigDecimalList.sum / bigDecimalList.length
  }

  private def calculateBestAverageRatings(
      documents: Seq[Document],
      minReviews: Int,
      limit: Int
  ): IO[Seq[ProductAverageRating]] = {
    IO {
      documents
        .map(d => jawn.decode[ReviewRatings](d.toJson()))
        .filter(p => p.toOption.get.overallList.length >= minReviews)
        .map { case Right(p) =>
          ProductAverageRating(
            p._id,
            averageAsBigDecimal(p.overallList)
          )
        }
        .sortBy(r => r.averageRating)(Ordering.BigDecimal.reverse)
        .take(limit)
    }
  }

  def findBestReviews(
      bestReviewRequest: BestReviewRequest
  ): Either[Throwable, Seq[ProductAverageRating]] = {
    (for {
      asinAndOverallList <- reviewRepository
        .getGroupedReviewRatings(
          bestReviewRequest.start,
          bestReviewRequest.end
        )
      productRatings <- calculateBestAverageRatings(
        asinAndOverallList,
        bestReviewRequest.min,
        bestReviewRequest.limit
      )
    } yield productRatings).attempt.unsafeRunSync()
  }

}
