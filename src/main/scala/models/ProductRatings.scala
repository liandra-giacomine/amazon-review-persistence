package models

import io.circe.generic.semiauto.deriveDecoder
import io.circe.Decoder

case class ProductRatings(_id: String, overallList: List[Double])

object ProductRatings {
  implicit val decoder: Decoder[ProductRatings] = deriveDecoder[ProductRatings]
}
