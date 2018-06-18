package info.xiaoc.spring.reactive.test

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class SavePersonTest extends Simulation {

  def randomString(length: Int) = scala.util.Random.alphanumeric.take(length).mkString

  val httpProtocol = http
    .baseURL("http://localhost:8080")
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("deflate, gzip")
    .contentTypeHeader("application/json")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")


  val uri1 = "http://localhost:8080/person"

  val scn = scenario("SavePersonTest")
    .exec(http("CreatePerson")
      .post("/person")
      .body(StringBody(session => "{\"name\": \"" + randomString(4) + "\"}")).asJSON
      .check(status.is(200)))


  setUp(scn.inject(atOnceUsers(100))).protocols(httpProtocol)
}