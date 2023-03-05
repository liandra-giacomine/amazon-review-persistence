package amazonreviewpersistance

import cats.effect.IOApp

object Main extends IOApp.Simple {

  // TODO: Use secure data exchange (because there are peoples names in the data etc) https: //http4s.org/v0.23/docs/hsts.html

  Server.cleanUpDB.unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  Server.loadData.unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  val run = Server.run
}
