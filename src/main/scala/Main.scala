package amazonreviewpersistance

import cats.effect.IOApp

object Main extends IOApp.Simple {

  // TODO: Use secure data exchange (because there are peoples names in the data etc) https: //http4s.org/v0.23/docs/hsts.html

  Server.cleanUpDB.unsafeRunSync()(cats.effect.unsafe.IORuntime.global) match {
    case Left(e) =>
      println("Failed to clean up the DB: " + e.getMessage)
      e
    case Right(r) => r
  }

  Server.loadData.unsafeRunSync()(cats.effect.unsafe.IORuntime.global) match {
    case Left(e) =>
      println("Failed to load data to the DB: " + e.getMessage)
      e
    case Right(r) => r
  }

  val run = Server.run
}
