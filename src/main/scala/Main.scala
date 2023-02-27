package amazonreviewpersistance

import cats.effect.{IO, IOApp}
import models.Review

object Main extends IOApp.Simple {
  println("starting service!!")
  PersistenceService
    .cleanCollection()
    .unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  val len = ReviewService
    .insertReviewsFromFile(
      ReviewFile.path.get
    )
    .unsafeRunSync()(
      cats.effect.unsafe.IORuntime.global
    )
    .length // TODO: Need to handle when path is not passed

  println("LENGTH OF LIST OF REVIEWS!!!!!!!!: " + len)
//  implicit val runTimeGlobal = cats.effect.unsafe.IORuntime.global
//
//  PersistenceService
//    .insertReview(
//      Review("1", List(1, 2), "4.5", "a", "b", "c", "d", 1234567898)
//    )
//    .unsafeRunSync()

  val run = Server.run[IO]
}
