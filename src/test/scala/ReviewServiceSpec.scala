package amazonreviewpersistance

import cats.effect.IO
import models.requests.BestReviewRequest
import models.responses.ReviewRating
import munit.CatsEffectSuite
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mongodb.scala.Document
import org.scalatest.matchers.must.Matchers.{a, convertToAnyMustWrapper}
import org.scalatestplus.mockito.MockitoSugar.mock
import services.{ReviewRepository, ReviewService}

class ReviewServiceSpec extends CatsEffectSuite {

  val mockRepository = mock[ReviewRepository]
  val reviewService  = new ReviewService(mockRepository)

  val bestReviewRequest = BestReviewRequest(1000000000, 1000000000, 1, 1)

  test(
    "Returns a sequence of ReviewRating from the sequence of documents returned by the repository"
  ) {
    when(mockRepository.getBestReviews(any(), any()))
      .thenReturn(IO(Seq.empty[Document]))

    val either = reviewService.findBestReviews(bestReviewRequest)

    either mustBe Right(Seq.empty[ReviewRating])
  }

  test(
    "When an exception is thrown by the repository, the ReviewService returns it in a Left"
  ) {
    when(mockRepository.getBestReviews(any(), any()))
      .thenReturn(IO(throw new Exception("test")))

    val either = reviewService.findBestReviews(bestReviewRequest)

    either.left.toOption.get mustBe a[Throwable]
  }
}
