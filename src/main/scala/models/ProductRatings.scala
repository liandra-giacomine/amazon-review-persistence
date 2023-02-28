package models

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Encoder, Json}

case class ProductRatings(_id: String, overallList: List[Double])

object ProductRatings {
  implicit val decoder: Decoder[ProductRatings] = deriveDecoder[ProductRatings]
}
