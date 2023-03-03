package amazonreviewpersistance

import cats.effect.IO
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._

object Server {

  val run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpApp(Routes.reviewRoutes.orNotFound)
      .build
      .use(_ => IO.never)
      .as(())
}
