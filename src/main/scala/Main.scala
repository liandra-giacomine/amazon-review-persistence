package amazonreviewpersistance

import cats.effect.IOApp
import services.{ReviewService, ReviewsRepository}

object Main extends IOApp.Simple {

  // TODO: Use secure data exchange (because there are peoples names in the data etc) https: //http4s.org/v0.23/docs/hsts.html

  ReviewsRepository
    .cleanCollection()

  (ReviewFile.inputValue match {
    case Some(f) =>
      ReviewService.insertReviewsFromFile(f)
    case None =>
      ReviewService
        .insertReviewsFromFile("src/main/scala/resources/reviews.json")
  }).unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  val run = Server.run
}
