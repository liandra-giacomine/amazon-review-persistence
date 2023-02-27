# Amazon Review Persistence

This microservice stores amazon review data from a configurable JSON file, whose filepath is supplied when starting the application, and stores it to a collection in MongoDB. This then allows for efficient querying of the review data.

### Specifying your filepath

When starting the service, you will need to specify the file path for the review data.

This is done through the command line parameter `-Dfilepath`. 

Here's an example of the full command when starting the application `sbt run -Dfilepath="<yourFilePath>"`

### Prerequisites

This service uses the Mongo Scala Driver version 4.8, which requires your MongoDB version to be at least 3.6 [https://www.mongodb.com/docs/drivers/scala/#compatibility]

