package amazonreviewpersistance

import models.responses.ProductAverageRating
import munit.CatsEffectSuite
import repositories.ReviewRepository

import java.nio.file.Paths

class ReviewRepositorySpec extends CatsEffectSuite {

  
  test(
    "Returns a sequence of ReviewRating from the sequence of documents returned by the repository"
  ) {
    when(mockRepository.getGroupedReviewRatings(any(), any()))
      .thenReturn(IO(Seq.empty[Document]))

    val either = reviewService.findBestReviews(bestReviewRequest)

    either mustBe Right(Seq.empty[ProductAverageRating])
  }

  test(
    "When an exception is thrown by the repository, the ReviewService returns it in a Left"
  ) {
    when(mockRepository.getGroupedReviewRatings(any(), any()))
      .thenReturn(IO(throw new Exception("test")))

    val either = reviewService.findBestReviews(bestReviewRequest)

    either.left.toOption.get mustBe a[Throwable]
  }
}
