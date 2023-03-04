package amazonreviewpersistance

import cats.effect.IO
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import services.{ReviewRepository, ReviewService}
import utils.ReviewFile

object Server {
  implicit val repository: ReviewRepository = new ReviewRepository

  val reviewService = new ReviewService(repository)

  val setUp = IO {
    repository
      .cleanCollection()

    ReviewFile.inputValue match {
      case Some(f) =>
        reviewService.insertReviewsFromFile(f)
      case None =>
        reviewService
          .insertReviewsFromFile("src/main/scala/resources/reviews.json")
    }
  }

  private val routes = new Routes(reviewService)

  val run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpApp(routes.reviewRoutes.orNotFound)
      .build
      .use(_ => IO.never)
      .as(())
}
