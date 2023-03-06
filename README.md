# Amazon Review Persistence

This microservice stores amazon review data from a configurable JSON file, whose filepath is supplied when starting the application, and stores it to a collection in MongoDB. This then allows for efficient querying of the review data.

## Prerequisites

- SBT
- Java 11+
- Mongo 3.6+

## Build and run

This service runs on port 8081.

### Specifying your filepath

When starting the service, you will need to specify the file path for the review data.

This is done through the command line parameter `-Dfilepath`.

Here's an example of the full command when starting the application `sbt compile run -Dfilepath="<yourFilePath>"`

Assumption: If no filepath is specified, a default file is used. To compile and run this service with the default file, use the following command: `sbt compile run`


### Testing

- To run unit tests use `sbt test`
- To run integration tests use `sbt it:test`

### Libraries

- http4s: Automatically streams requests using fs2
- fs2: Functional streaming
- MongoDB: Storing and querying review data

#### Assumptions

- Default file used if no filepath is specified
- Random product returned for two products with the same rating and the array size in the response would be larger than the limit in the request
- A review is ignored if it cannot be parsed
