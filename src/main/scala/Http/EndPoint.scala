package Http

import cats.Eq
import zio.Has
import zio.ZIO
import uzhttp.Request
import uzhttp.HTTPError
import uzhttp.Response
import zio.URIO
import zio.IO
import cats.data.NonEmptyList
import org.json4s.JsonAST
import uzhttp.header.Headers
import org.json4s.jackson.JsonMethods
import zio.Task
import zio.RIO
import zio.stream.ZTransducer
import zio.UIO
import uzhttp.Status
import zio.ZLayer
import uzhttp.server.Server

import java.net.InetSocketAddress
import zio.ZManaged
import zio.blocking.Blocking
import zio.clock.Clock
import cats.implicits._
import uzhttp.Request.Method





object EndPoint{

    implicit lazy val methodEq: Eq[Method] = { (a, b) => a == b }
    type HRequest = Has[Request]

    type EndPoint[R <: HRequest] = ZIO[R, Option[HTTPError], Response]

    val ApplicationJson = "application/json"


    def uri: ZIO[HRequest, Nothing, List[String]] = request.map(r => r.uri.getPath.split("/").toList.filterNot(_ == ""))
    def request: URIO[HRequest, Request] = ZIO.access[HRequest](_.get)
    def method: ZIO[HRequest, Nothing, Request.Method] = request.map(_.method)


    def uriMethod(expectedMethod: Request.Method): ZIO[HRequest, Option[HTTPError], Unit] = {
    for {
      pth <- uri
      mtd <- method
      matched <- if  (mtd === expectedMethod)
        IO.unit else IO.fail(None)
    } yield matched
  }

   def combineRoutes[R <: HRequest](h: EndPoint[R], t: EndPoint[R]*): EndPoint[R] =
    t.foldLeft(h)((acc, it) =>
      acc catchSome { case None => it }
    )

   def jsonResponse(jv: JsonAST.JValue): Response =
    Response.plain(JsonMethods.pretty(jv), Status.Ok, List(Headers.ContentType -> ApplicationJson))


  def authLayerWM[R](
    pz: RIO[R, EndPoint[HRequest]],
    port: Int
  ): ZLayer[Any with Blocking with Clock with R, Throwable, Has[Server]] = ZLayer.fromManaged {
    val zm = for {
      p <- pz
      
      h = Server
          .builder(new InetSocketAddress("127.0.0.1", port))
          .handleAll(handler(p))
          .serve
                
    } yield h
    ZManaged.unwrap(zm)
  }
  def findOrNot(p: EndPoint[HRequest]) =
    for {
      r <- request
      pp <- p.mapError {
            case Some(err) => err
            case None => HTTPError.NotFound(r.uri.getPath())
      }
    } yield pp

  def handler(p: EndPoint[HRequest]): Request => ZIO[Any, HTTPError, Response]= {
    val f = { req: Request =>
        (for {
          res      <- findOrNot(p).provideLayer(ZLayer.succeed(req))
        } yield res).mapError { th =>
          th match {
            case herr: HTTPError => herr
            case th              => HTTPError.Unauthorized(th.getMessage)
          }
        } 
    }
    f
  }

}