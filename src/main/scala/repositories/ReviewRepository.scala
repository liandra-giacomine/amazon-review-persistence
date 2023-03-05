package repositories

import cats.effect.IO
import models.ReviewDocument
import org.bson.codecs.configuration.CodecRegistries.{
  fromProviders,
  fromRegistries
}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.{Accumulators, Indexes}
import org.mongodb.scala.model.Aggregates.{filter, group}
import org.mongodb.scala.model.Filters.{exists, gte, lte}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.ExecutionContext

class ReviewRepository(implicit val ec: ExecutionContext) {

  private lazy val codecRegistry = fromRegistries(
    fromProviders(classOf[ReviewDocument]),
    DEFAULT_CODEC_REGISTRY
  )

  private val mongoClient = MongoClient()

  private val database: MongoDatabase = mongoClient
    .getDatabase("amazon_reviews_db")
    .withCodecRegistry(codecRegistry)

  val collection: MongoCollection[ReviewDocument] =
    database.getCollection("reviews_collection")

  collection.createIndex(
    Indexes
      .compoundIndex(
        Indexes.ascending("asin"),
        Indexes.ascending("unixReviewTime"),
        Indexes.ascending("overall")
      )
  )

  def cleanCollection() =
    IO.fromFuture(
      IO(
        collection
          .deleteMany(Document())
          .toFuture()
      )
    ).map(_ => ())

  def insertReview(reviews: Vector[ReviewDocument]) = {
    IO.fromFuture(
      IO(
        collection
          .insertMany(reviews)
          .toFuture()
      )
    )
  }.map(_ => ())

  def getGroupedReviewRatings(
      startTime: Long,
      endTime: Long,
      minReviews: Int
  ) = {
    println(startTime)
    println(endTime)
    IO.fromFuture(
      IO(
        collection
          .aggregate[Document](
            List(
              filter(gte("unixReviewTime", startTime)),
              filter(lte("unixReviewTime", endTime)),
              group(
                "$asin",
                Accumulators.push("overallList", "$overall")
              ),
              filter(exists(s"overallList.${minReviews - 1}"))
            )
          )
          .toFuture()
          .map(_.toVector)
          .recover { case e =>
            println("Mongo Error: " + e.getMessage)
            Vector.empty[Document]
          }
      )
    )
  }
}
