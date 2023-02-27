package amazonreviewpersistance

import cats.data.EitherT
import cats.effect.IO
import models.{Review, ReviewDocument}
import models.errors.MongoError
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{
  fromProviders,
  fromRegistries
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
    IO(collection.deleteMany(Document()).toFutureOption()).map(_ => ())

  def insertReview(r: ReviewDocument): IO[Unit] = {
    IO(
      collection
        .insertOne(r)
    )
  }

  def getBestReviews(
      fromTimeStamp: Long,
      toTimeStamp: Long,
      minReviews: Int,
      limit: Int
  ) = {
    IO.fromFuture(IO(collection.find().first().toFutureOption()))
  }

}
