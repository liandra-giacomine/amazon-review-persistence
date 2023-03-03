package models.requests

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, HCursor, Json}

case class BestReviewRequest(
    startDate: String,
    endDate: String,
    limit: Int,
    minReviews: Int
)

object BestReviewRequest {
  implicit val decoder: Decoder[BestReviewRequest] =
    new Decoder[BestReviewRequest] {
      final def apply(c: HCursor): Decoder.Result[BestReviewRequest] =
        for {
          startDate  <- c.downField("start").as[String]
          endDate    <- c.downField("end").as[String]
          limit      <- c.downField("limit").as[Int]
          minReviews <- c.downField("min_number_reviews").as[Int]
        } yield {
          BestReviewRequest(startDate, endDate, limit, minReviews)
        }
    }

  implicit val encode: Encoder[BestReviewRequest] =
    new Encoder[BestReviewRequest] {
      final def apply(b: BestReviewRequest): Json = Json.obj(
        ("start", Json.fromString(b.startDate)),
        ("end", Json.fromString(b.endDate)),
        ("limit", Json.fromInt(b.limit)),
        ("minReviews", Json.fromInt(b.minReviews))
      )
    }
}
