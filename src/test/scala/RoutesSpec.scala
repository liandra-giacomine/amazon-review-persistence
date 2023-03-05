package amazonreviewpersistance

import cats.effect.IO
import io.circe.Json
import models.responses.ProductAverageRating
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mongodb.scala.Document
import org.scalatestplus.mockito.MockitoSugar.mock
import repositories.ReviewRepository
import services.ReviewService

class RoutesSpec extends CatsEffectSuite {

  val mockRepository = mock[ReviewRepository]
  val reviewService  = new ReviewService(mockRepository)

  val routes = new Routes(reviewService)

  implicit val encoderReviewRating
      : EntityEncoder[IO, Seq[ProductAverageRating]] =
    jsonEncoderOf[IO, Seq[ProductAverageRating]]

  implicit val decoder: EntityDecoder[IO, Seq[ProductAverageRating]] =
    jsonOf[IO, Seq[ProductAverageRating]]

  val validPayload = Json
    .fromFields(
      List(
        ("start", Json.fromString("01.01.2010")),
        ("end", Json.fromString("01.01.2020")),
        ("limit", Json.fromInt(1)),
        ("min", Json.fromInt(1))
      )
    )
    .toString

  private def getBestReview(payload: String): IO[Response[IO]] =
    routes.reviewRoutes.orNotFound
      .run(
        Request(
          method = Method.POST,
          uri = uri"/reviews/best"
        ).withEntity(payload)
      )

  val validPayloadRequest = getBestReview(validPayload)

  test(
    "POST /reviews/best returns status code Ok given a successful response from the repository"
  ) {
    when(mockRepository.getGroupedReviewRatings(any(), any()))
      .thenReturn(IO(Seq.empty[Document]))

    assertIO(validPayloadRequest.map(_.status), Status.Ok)
  }

  test(
    "POST /reviews/best returns status code a sequence of ReviewRating given a successful response from the repository"
  ) {
    when(mockRepository.getGroupedReviewRatings(any(), any()))
      .thenReturn(IO(Seq.empty[Document]))

    assertIO(
      validPayloadRequest.flatMap(r => r.as[Seq[ProductAverageRating]]),
      Seq.empty[ProductAverageRating]
    )
  }

  test(
    "POST /reviews/best returns BadRequest when an exception is thrown in the repository"
  ) {
    val invalidJsonRequest = getBestReview("Invalid json")
    assertIO(
      invalidJsonRequest.map(_.status),
      Status.BadRequest
    )

    assertIO(
      invalidJsonRequest.flatMap(_.as[String]),
      "Malformed message body: Invalid JSON"
    )
  }

  test(
    "POST /reviews/best returns InternalServerError when an exception is thrown in the repository"
  ) {
    when(mockRepository.getGroupedReviewRatings(any(), any()))
      .thenReturn(IO(throw new Exception("test")))

    assertIO(validPayloadRequest.map(_.status), Status.InternalServerError)
  }

}
