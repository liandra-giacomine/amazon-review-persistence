package models.requests

import io.circe.{Decoder, Encoder, HCursor, Json}

case class BestReviewRequest(
    start: Long,
    end: Long,
    limit: Int,
    min: Int
)

object BestReviewRequest {

  private val dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy")
  private def convertToUnixTimestamp(dateStr: String): Long = {
    dateFormat
      .parse(dateStr)
      .getTime / 1000 // convert to seconds from milliseconds as per the representation in the review documents in DB
  }

  implicit val decoder: Decoder[BestReviewRequest] =
    new Decoder[BestReviewRequest] {
      final def apply(c: HCursor): Decoder.Result[BestReviewRequest] =
        for {
          startDate <- c
            .downField("start")
            .as[String]
            .map(convertToUnixTimestamp)
          endDate <- c.downField("end").as[String].map(convertToUnixTimestamp)
          limit   <- c.downField("limit").as[Int]
          minReviews <- c.downField("min").as[Int]
        } yield {
          BestReviewRequest(startDate, endDate, limit, minReviews)
        }
    }

  implicit val encode: Encoder[BestReviewRequest] =
    new Encoder[BestReviewRequest] {
      final def apply(b: BestReviewRequest): Json = Json.obj(
        ("start", Json.fromLong(b.start)),
        ("end", Json.fromLong(b.end)),
        ("limit", Json.fromInt(b.limit)),
        ("minReviews", Json.fromInt(b.min))
      )
    }
}
