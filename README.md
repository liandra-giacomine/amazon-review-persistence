# Amazon Review Persistence

This microservice stores amazon review data from a configurable JSON file, whose filepath is supplied when starting the application, and stores it to a collection in MongoDB. This then allows for efficient querying of the review data.

## Prerequisites

SBT is required to compile the source and Java 17+ is required for the mongo driver dependency.

MongoDB should be running before starting this service. 

This uses version 4.8 of the Mongo Scala Driver, which requires your MongoDB version to be at least 3.6 [Click here to check mongo compatibility](https://www.mongodb.com/docs/drivers/scala/#compatibility)

## Build and run

This service runs on port 8081. You can compile and run it with the following command `sbt compile run`

### Specifying your filepath

When starting the service, you will need to specify the file path for the review data.

This is done through the command line parameter `-Dfilepath`.

Here's an example of the full command when starting the application `sbt run -Dfilepath="<yourFilePath>"`