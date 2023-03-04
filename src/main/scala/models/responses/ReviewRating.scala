package models.responses

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Encoder, Json}
import models.ProductRatings

case class ReviewRating(asin: String, averageRating: BigDecimal)

object ReviewRating {
  implicit val encoder: Encoder[ReviewRating] = new Encoder[ReviewRating] {
    final def apply(bestRated: ReviewRating): Json = Json.obj(
      ("asin", Json.fromString(bestRated.asin)),
      ("average_rating", Json.fromBigDecimal(bestRated.averageRating))
    )
  }

  implicit val decoder: Decoder[ReviewRating] = deriveDecoder[ReviewRating]
}
