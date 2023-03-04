package amazonreviewpersistance

import cats.effect.IO
import models.{ReviewDocument}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{
  fromRegistries,
  fromProviders
}
import org.mongodb.scala.model.Accumulators
import org.mongodb.scala.model.Aggregates.{filter, group}
import org.mongodb.scala.model.Filters.{gte, lte}
import org.mongodb.scala.bson.codecs.Macros._

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
      startTime: Long,
      endTime: Long
  ) = {
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
