package uk.co.danielrendall.saas.server

import org.specs2.mutable.Specification

import java.io.{File, FileInputStream}

class ServiceFinderSpec extends Specification:

  // TODO - remove this
  val file: File = new File("/home/daniel/Development/hello-world-as-a-service/target/scala-3.1.0/hello-world-as-a-service-assembly-1.0.0-SNAPSHOT.jar")

  val bytes: Array[Byte] = StreamUtils.toByteArray(new FileInputStream(file), file.length().toInt)

  "ServiceFinder" should {
    "Run the service" in {
      ServiceFinder.getService(bytes).getMetadata.name must be_==("HelloWorld")
    }
  }
