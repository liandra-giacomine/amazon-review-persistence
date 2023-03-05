package utils

import cats.effect.IO
import fs2.io.file.NoSuchFileException
import munit.CatsEffectSuite
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.{a, convertToAnyMustWrapper}
import org.scalatestplus.mockito.MockitoSugar.mock
import repositories.ReviewRepository

class DBStreamingParserSpec extends CatsEffectSuite {

  val mockRepository    = mock[ReviewRepository]
  val dbStreamingParser = new DBStreamingParser(mockRepository)

  val streamingToDBResult = dbStreamingParser.fileToDatabaseStream(
    "src/main/scala/resources/reviews.json"
  )

  test(
    "Returns a right of unit when streaming from file to DB completes successfully"
  ) {
    when(mockRepository.insertReview(any()))
      .thenReturn(IO(()))

    assertIO(streamingToDBResult, Right(()))
  }

  test(
    "Returns a left of throwable when it receives an exception from the repository"
  ) {
    when(mockRepository.insertReview(any()))
      .thenReturn(IO(throw new Exception("test")))

    streamingToDBResult.unsafeRunSync().left.toOption.get mustBe a[Exception]
  }

  test(
    "Returns a left of throwable when it receives an invalid filepath"
  ) {
    dbStreamingParser
      .fileToDatabaseStream(
        "invalid filepath"
      )
      .unsafeRunSync()
      .left
      .toOption
      .get mustBe a[NoSuchFileException]
  }

}
