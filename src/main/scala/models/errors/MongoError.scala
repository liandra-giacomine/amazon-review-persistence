package models.errors

sealed abstract class MongoError(message: String)

object MongoError {
  case object DeleteAllError
      extends MongoError("Failed to delete all documents in collection.")
}
