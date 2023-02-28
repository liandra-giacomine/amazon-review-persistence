package amazonreviewpersistance

import cats.effect.{IO, IOApp}
import models.Review

object Main extends IOApp.Simple {

  // TODO: Use secure data exchange (because there are peoples names in the data etc) https: //http4s.org/v0.23/docs/hsts.html

  PersistenceService
    .cleanCollection()
    .unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  ReviewService
    .insertReviewsFromFile()
    .unsafeRunSync()(
      cats.effect.unsafe.IORuntime.global
    )
  // TODO: Need to handle when path is not passed

  val run = Server.run[IO]
}
