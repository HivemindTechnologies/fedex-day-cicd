package Http

import zio.{Has, Ref, Task, ZIO, ZLayer, clock}
import uzhttp.Request
import EndPoint._
import Data.Dataframe.combine
import org.apache.spark.sql.SparkSession
import org.json4s.JValue
import uzhttp.Response
import uzhttp.server.Server
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration.durationInt

object HttpService{
  def service(path: Option[String], request: String, spark: SparkSession): ZIO[Any, Throwable, HttpService] = {
    for {
      response <- combine(request, path, spark).fork
      join <- response.join
      res <- Ref.make(join)
    } yield HttpService(res)
  }

  def serverLayer(path: Option[String], request: String, sparkSession: SparkSession): ZLayer[Any with Blocking with Clock, Throwable, Has[Server]] =
    EndPoint.authLayerWM(
    {
      for{
        s <- service(path, request, sparkSession)
      } yield
        combineRoutes(s.getResult, s.postIn)
    }, 8080)

}

case class HttpService(resultJson: Ref[JValue]){

  def postIn: EndPoint[HRequest] =
    for{
      _ <- uriMethod(Request.Method.POST)
    } yield Response.plain("OK")

  def getResult: EndPoint[HRequest] = 
    for{
      _ <- uriMethod(Request.Method.GET)
      res <- resultJson.get
    } yield {
      jsonResponse(res)
    }
}

object Utils {

  type UZServer = Has[Server]

  def serverUp: ZIO[UZServer, Nothing, Task[Unit]] = ZIO.access[UZServer](_.get).map(_.awaitUp)

}