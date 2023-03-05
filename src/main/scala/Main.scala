package amazonreviewpersistance

import cats.effect.IOApp
import services.{ReviewRepository, ReviewService}
import utils.ReviewFile

object Main extends IOApp.Simple {

  // TODO: Use secure data exchange (because there are peoples names in the data etc) https: //http4s.org/v0.23/docs/hsts.html

  Server.cleanUpDB.unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  Server.loadData.unsafeRunSync()(cats.effect.unsafe.IORuntime.global) match {
    case Left(e)  => println("ERRRORRR: " + e.getMessage)
    case Right(r) => println("I DID IIIIT!!")
  }

  val run = Server.run
}
