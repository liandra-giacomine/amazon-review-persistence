package amazonreviewpersistance

import fs2.io.file.{Files, Path}
import fs2.{Pipe, Stream, text}
import cats.effect.IO
import io.circe.parser._
import models.{Review, ReviewDocument, ReviewRating}

object ReviewService {

  implicit val runtime = cats.effect.unsafe.IORuntime.global

  def insertReviewsFromFile(readFrom: String) = {
    val fs2Path = Path.fromNioPath(
      java.nio.file.Paths.get(readFrom)
    )

    println("In insert reviews!!")

    Files[IO]
      .readAll(fs2Path)
      .through(text.utf8.decode)
      .through(text.lines)
      .evalMap(line => IO(parse(line)))
      .filter(jsonEither => jsonEither.isRight)
      .evalMap(json => IO(json.toOption.get.as[Review].toOption))
      .filter(reviewOpt => reviewOpt.isDefined)
      .evalMap(review => IO(ReviewDocument(review.get)))
      //     .filter(x => x.isRight)
      .evalMap(reviewDocument =>
        IO(PersistenceService.insertReview(reviewDocument))
      )
      .compile
      .drain

//      .flatMap { json =>
//        json
//          .as[Review]
//          .toOption
//          .map(r => PersistenceService.insertReview(r).unsafeRunSync())
//          .getOrElse(())
//      }
//      .compile
    // TODO: handle error

//    PersistenceService
//      .insertReview(
//        Review("2", List(1, 2), "4.5", "a", "b", "c", "d", 1234567898)
//      )
//      .unsafeRunSync()
  }
}
