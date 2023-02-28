package amazonreviewpersistance

import cats.effect.IO
import models.{Review, ReviewDocument}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{
  fromProviders,
  fromRegistries
}
import org.mongodb.scala.bson.{BsonDocument, BsonObjectId}
import org.mongodb.scala.model.Accumulators
import org.mongodb.scala.model.Accumulators.avg
import org.mongodb.scala.model.Aggregates.{count, filter, group, project}
import org.mongodb.scala.model.Filters.{equal, gte, lte}
import org.mongodb.scala.model.Projections.{
  computed,
  exclude,
  excludeId,
  fields,
  include
}

object PersistenceService {

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  lazy val codecRegistry = fromRegistries(
    fromProviders(classOf[ReviewDocument]),
    DEFAULT_CODEC_REGISTRY
  )

  val mongoClient = MongoClient()

  val database: MongoDatabase = mongoClient
    .getDatabase("amazon_reviews_db")
    .withCodecRegistry(codecRegistry)

  val collection: MongoCollection[ReviewDocument] =
    database.getCollection("reviews_collection")

  def cleanCollection(): IO[Unit] =
    IO.fromFuture(IO(collection.deleteMany(Document()).toFuture()))
      .map(_ => ())

  def insertReview(reviews: List[ReviewDocument]): IO[Unit] = {
    IO.fromFuture(
      IO(
        collection
          .insertMany(reviews)
          .toFuture()
      )
    ).map(_ => ())
  }

  def getBestReviews(
      fromTimeStamp: Long,
      toTimeStamp: Long,
      minReviews: Int,
      limit: Int
  ) = {
    IO.fromFuture(
      IO(
        collection
          .aggregate[Document](
            List(
              filter(gte("unixReviewTime", fromTimeStamp)),
              filter(lte("unixReviewTime", toTimeStamp)),
              group(
                "$asin",
                Accumulators.push("overallList", "$overall")
              )
//              project(
//                fields(
//                  include("_id", "overallSum")
//                )
//              )
            )
          )
          .toFuture()
          .recover { case e =>
            println("Mongo Error: " + e.getMessage)
            Seq.empty[Document]
          }
      )
    )
  }

  // ase class ReviewDocument(
  //    _id: ObjectId,
  //    asin: String,
  //    helpful: List[Int],
  //    overall: Double,
  //    reviewText: String,
  //    reviewerID: String,
  //    reviewerName: String,
  //    summary: String,
  //    unixReviewTime: Long
}
