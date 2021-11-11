package Http

import Http.HttpClient.HttpClient
import org.apache.spark.sql.SparkSession
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.duration.durationInt
import zio.magic.ZioProvideMagicOps
import zio.{App, ExitCode, UIO, ZIO, clock}

object ServerUp extends App {
  override def run(args: List[String]) = {
    lazy val adminString ="""{
                            |    "start": "01.01.2010",
                            |    "end": "31.12.2020",
                            |    "limit": 2,
                            |    "min_number_reviews": 2
                            |}""".stripMargin
    val requestString = args.headOption
      .getOrElse(adminString)
    val spark: SparkSession = SparkSession.builder()
      .master("local[3]")
      .appName("Webservice")
      .getOrCreate()
    serverUp.injectCustom(
      HttpService.serverLayer(None, requestString, spark),
      HttpClient.clientLayer,
      AsyncHttpClientZioBackend.layer()).fold(exception => {
      val exc = exception.asInstanceOf[Exception]
      exc.printStackTrace()
      ExitCode(1)
    }, _ => ExitCode(0))

  }

  def serverUp = for{
    _ <- Utils.serverUp
    _ <- UIO(println("server is up"))
    _ <- clock.sleep(5000.seconds)
  } yield ()
}

