package amazonreviewpersistance

import cats.effect.{IO, IOApp}
import models.Review

object Main extends IOApp.Simple {
  println("starting service!!")
  PersistenceService
    .cleanCollection()
    .unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  val len = ReviewService
    .insertReviewsFromFile()
    .unsafeRunSync()(
      cats.effect.unsafe.IORuntime.global
    )
  // TODO: Need to handle when path is not passed

  println("LENGTH OF LIST OF REVIEWS!!!!!!!!: " + len)

  val run = Server.run[IO]
}
