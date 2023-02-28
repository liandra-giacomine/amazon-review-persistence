package models

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax.EncoderOps

case class BestReviewResponse(asin: String, averageRating: BigDecimal)
object BestReviewResponse {
  implicit val decoder: Decoder[BestReviewResponse] =
    deriveDecoder[BestReviewResponse]
  implicit val encoder: Encoder[BestReviewResponse] =
    deriveEncoder[BestReviewResponse]
}
