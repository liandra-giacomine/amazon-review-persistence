package models.responses

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Encoder, Json}

case class ProductAverageRating(asin: String, averageRating: BigDecimal)

object ProductAverageRating {
  implicit val encoder: Encoder[ProductAverageRating] =
    new Encoder[ProductAverageRating] {
      final def apply(bestRated: ProductAverageRating): Json = Json.obj(
        ("asin", Json.fromString(bestRated.asin)),
        ("average_rating", Json.fromBigDecimal(bestRated.averageRating))
      )
    }

  implicit val decoder: Decoder[ProductAverageRating] =
    deriveDecoder[ProductAverageRating]
}
