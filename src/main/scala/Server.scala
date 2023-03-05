package amazonreviewpersistance

import cats.effect.IO
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import repositories.ReviewRepository
import services.ReviewService
import utils.{DBStreamingParser, ReviewFile}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object Server {
  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newScheduledThreadPool(4))

  implicit val runtime = cats.effect.unsafe.IORuntime.global

  private val repository: ReviewRepository = new ReviewRepository

  private val reviewService = new ReviewService(repository)

  private val dbStreamingParser = new DBStreamingParser(repository)

  val cleanUpDB = repository.cleanCollection()

  val loadData =
    ReviewFile.inputValue match {
      case Some(filepath) =>
        dbStreamingParser.fileToDatabaseStream(filepath)
      case None =>
        dbStreamingParser
          .fileToDatabaseStream("src/main/scala/resources/reviews.json")
    }

  private val routes = new Routes(reviewService)

  val run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpApp(routes.reviewRoutes.orNotFound)
      .build
      .use(_ => IO.never)
      .as(())
}
