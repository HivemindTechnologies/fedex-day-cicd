package Http

import Http.HttpClient.HttpClient
import Http.Utils.UZServer
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{ExitCode, clock}
import zio.duration.durationInt
import zio.magic.ZioProvideMagicOps
import zio._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import zio.clock.Clock

import scala.concurrent.Future

object ServiceApp extends App {
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
    import spark.implicits._

    serverUp.injectCustom(
        HttpService.serverLayer(None, requestString, spark),
      HttpClient.clientLayer,
      Clock.live,
      AsyncHttpClientZioBackend.layer()).fold(exception => {
      val exc = exception.asInstanceOf[Exception]
      exc.printStackTrace()
      ExitCode(1)
    }, _ => ExitCode(0))

  }
//  you can import it like this:
//    "com.leobenkel"           %%  "zparkio"                     % s"$sparkVersion_zsparkIoVersion"
//
//  e.g.
//  "com.leobenkel"           %%  "zparkio"                     % s"3.1.2_0.14.1"

  def serverUp: ZIO[Clock with UZServer, Nothing, Unit] = for{
    _ <- Utils.serverUp
    _ <- UIO(println("server is up"))
    _ <- clock.sleep(5000.seconds)
  } yield ()

  def get(s: String) = for{
    _ <- ZIO.accessM[HttpClient](_.get.postIn("product", s))
    p <- ZIO.accessM[HttpClient](_.get.get("product"))
    _ <- UIO(println(p))
  } yield ()

  def program(s: String) = {
    for {
      _ <- serverUp
      _ <- get(s)
    } yield ()
  }

}
