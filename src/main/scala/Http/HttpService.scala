package Http

import DB.ScalaJdbcConnectSelect._
import Http.EndPoint._
import uzhttp.{Request, Response}
import uzhttp.server.Server
import zio.blocking.Blocking
import zio.clock.Clock
import zio.{Has, Task, ZIO, ZLayer}

object HttpService{
  def service = {
     println("go into this step")
     simpleTest
     HttpService()
  }

  def serverLayer: ZLayer[Any with Blocking with Clock, Throwable, Has[Server]] =
    EndPoint.authLayerWM(
    {
      val s = service
      combineRoutes(s.getResult, s.postIn)
    }, 8080)

}

case class HttpService(){

  def postIn: EndPoint[HRequest] =
    for{
      _ <- uriMethod(Request.Method.POST)
    } yield Response.plain("OK")

  def getResult: EndPoint[HRequest] = 
    for{
      _ <- uriMethod(Request.Method.GET)
    } yield Response.plain("OK")

}

object Utils {

  type UZServer = Has[Server]

  def serverUp: ZIO[UZServer, Nothing, Task[Unit]] = ZIO.access[UZServer](_.get).map(_.awaitShutdown)

}