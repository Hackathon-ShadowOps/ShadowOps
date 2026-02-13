# ShadowOps

ShadowOps is a Java web service built with the [Javalin](https://javalin.io/) framework and a [SQLite](https://www.sqlite.org/) database.

## Getting Started

1. Ensure you have a recent JDK installed (e.g. Java 17 or later).
2. Build the project using your chosen build tool (for example):
   - **Maven:** `mvn clean package`
   - **Gradle:** `gradle clean build`
3. Run the server using the built JAR, for example:
   - `java -jar build/libs/shadowops.jar`  
     or  
   - `java -jar target/shadowops.jar`

The application will start an HTTP server via Javalin and use a local SQLite database file for persistence.
