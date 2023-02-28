package models

import org.bson.codecs.pojo.annotations.BsonId
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.annotations.BsonProperty

case class ReviewDocument(
    _id: ObjectId,
    asin: String,
    helpful: List[Int],
    overall: Double,
    reviewText: String,
    reviewerID: String,
    reviewerName: String,
    summary: String,
    unixReviewTime: Long
)

object ReviewDocument {
  def apply(r: Review): ReviewDocument = {
    ReviewDocument(
      new ObjectId(),
      r.asin,
      List(r.helpful._1, r.helpful._2),
      r.overall,
      r.reviewText,
      r.reviewerID,
      r.reviewerName,
      r.summary,
      r.unixReviewTime
    )
  }
}
