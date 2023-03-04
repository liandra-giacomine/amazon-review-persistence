package amazonreviewpersistance

import cats.effect.IOApp
import services.{ReviewRepository, ReviewService}
import utils.ReviewFile

object Main extends IOApp.Simple {

  // TODO: Use secure data exchange (because there are peoples names in the data etc) https: //http4s.org/v0.23/docs/hsts.html

  Server.setUp
    .handleError { _ =>
      throw new Exception("Failed to load data into the database")
    }
    .unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  val run = Server.run
}
