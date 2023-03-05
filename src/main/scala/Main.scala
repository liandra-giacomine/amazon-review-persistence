package amazonreviewpersistance

import cats.effect.IOApp

object Main extends IOApp.Simple {

  Server.cleanUpDB.unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  Server.loadData.unsafeRunSync()(cats.effect.unsafe.IORuntime.global)

  val run = Server.run
}
