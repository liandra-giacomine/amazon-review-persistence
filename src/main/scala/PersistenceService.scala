package amazonreviewpersistance

import cats.effect.IO
import models.{ReviewDocument}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import io.circe.parser._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{
  fromProviders,
  fromRegistries
}
import org.mongodb.scala.model.Accumulators
import org.mongodb.scala.model.Aggregates.{filter, group}
import org.mongodb.scala.model.Filters.{gte, lte}

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
}
