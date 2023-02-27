package models

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax.EncoderOps

final case class Review(
    asin: String,
    helpful: (Int, Int),
    overall: BigDecimal,
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
