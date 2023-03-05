package amazonreviewpersistance

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.io._
import io.circe.syntax._
import org.http4s.circe._
import models.requests.BestReviewRequest
import services.ReviewService

import scala.concurrent.ExecutionContext

class Routes(reviewService: ReviewService)(implicit
    val ec: ExecutionContext,
    runtime: IORuntime
) {

  implicit val decoder: EntityDecoder[IO, BestReviewRequest] =
    jsonOf[IO, BestReviewRequest]

  val reviewRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "reviews" / "best" =>
      import org.http4s.dsl.io._

      req
        .as[BestReviewRequest]
        .attempt
        .map {
          case Left(thr) => BadRequest(thr.getMessage)
          case Right(bestReviewReq) =>
            reviewService.findBestReviews(bestReviewReq) match {
              case Left(_)  => InternalServerError()
              case Right(b) => Ok(b.asJson)
            }
        }
        .unsafeRunSync()
  }
}
