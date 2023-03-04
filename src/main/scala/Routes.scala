package amazonreviewpersistance

import cats.effect.{IO, Resource, Sync}
import cats.implicits._
import fs2.{Pipe, Pure, Stream, text}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import java.io.{File, FileInputStream}
import fs2.io.file.{Files, Path}
import org.http4s.server._
import cats.effect._
import cats.syntax.all._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.circe.syntax._
import org.http4s.circe._
import io.circe.parser._
import models.Review
import models.requests.BestReviewRequest

import concurrent.duration.DurationInt
import java.nio.file.Paths
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object Routes {

  // TODO: Throw a bad request if toTimeStamp is more than fromTimeStamp?
  // TODO: Handle errors coming from the service, maybe return EitherT[IO, Throwable, List[ReviewRating]]

  implicit val decoder: EntityDecoder[IO, BestReviewRequest] =
    jsonOf[IO, BestReviewRequest]

  val reviewRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "amazon" / "best-review" =>
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
