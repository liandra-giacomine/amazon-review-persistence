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
  def fromDocument(r: ReviewDocument): Review = {
    Review(
      r.asin,
      (r.helpful.head, r.helpful.last),
      r.overall,
      r.reviewText,
      r.reviewerID,
      r.reviewerName,
      r.summary,
      r.unixReviewTime
    )
  }
}
