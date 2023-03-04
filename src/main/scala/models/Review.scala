package models

import io.circe._
import io.circe.generic.semiauto._

final case class Review(
    asin: String,
    helpful: (Int, Int),
    overall: Double,
    reviewText: String,
    reviewerID: String,
    reviewerName: String,
    summary: String,
    unixReviewTime: Long
)

object Review {
  implicit val decoder: Decoder[Review] = deriveDecoder[Review]
  implicit val encoder: Encoder[Review] = deriveEncoder[Review]
}
