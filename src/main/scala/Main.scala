package amazonreviewpersistance

import cats.effect.{IO, IOApp}
import models.Review

object Main extends IOApp.Simple {
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
