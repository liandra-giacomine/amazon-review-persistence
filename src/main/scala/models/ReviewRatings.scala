package models

import io.circe.generic.semiauto.deriveDecoder
import io.circe.Decoder

case class ReviewRatings(_id: String, overallList: List[Double])

object ReviewRatings {
  implicit val decoder: Decoder[ReviewRatings] = deriveDecoder[ReviewRatings]
}
