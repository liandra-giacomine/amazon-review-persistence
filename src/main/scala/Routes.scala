package amazonreviewpersistance

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.io._
import io.circe.syntax._
import org.http4s.circe._
import models.requests.BestReviewRequest
import services.ReviewService

object Routes {

  implicit val decoder: EntityDecoder[IO, BestReviewRequest] =
    jsonOf[IO, BestReviewRequest]

  val reviewRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "reviews" / "best" =>
      import org.http4s.dsl.io._
      implicit val runtime = cats.effect.unsafe.IORuntime.global

      req
        .as[BestReviewRequest]
        .attempt
        .map {
          case Left(thr) => BadRequest(thr.getMessage)
          case Right(bestReviewReq) =>
            ReviewService
              .getBestReviews(bestReviewReq) match {
              case Left(e)  => InternalServerError()
              case Right(b) => Ok(b.asJson)
            }
        }
        .unsafeRunSync()
  }
}
