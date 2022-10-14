package Http

import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.zio.SttpClient
import sttp.client.{Response, SttpBackend, asString, basicRequest}
import sttp.model.{StatusCode, Uri}
import sttp.model.Uri._
import zio.{Has, IO, Task, UIO, ZIO, ZLayer}
import zio.stream._

object HttpClient{

  type HttpClient = Has[Service]

    trait StringEncoder[T] {
    def mediaType: String
    def encode(t: String): String
  }
   trait StringDecoder[T] {
    def accepts: String
    def decode(body: String): Task[String]
  }
  implicit def JsonStringEncoder[T]: HttpClient.StringEncoder[T] = JsonStringEncoder()
  implicit def JsonStringDecoder[T]: HttpClient.StringDecoder[T] = JsonStringDecoder()

  case class JsonStringDecoder[T]() extends HttpClient.StringDecoder[T] {
      override def accepts: String = "application/json"

      override def decode(body: String): Task[String] =
        // println(s"body is $body")
        ZIO
          .effect(body)
          .tapError(e => UIO(println(s"error parsing body $body\n${e.toString}")))
    }

    case class JsonStringEncoder[T]() extends HttpClient.StringEncoder[T] {
      override def mediaType: String = "application/json"

      override def encode(t: String): String = {
        t
      }
    }


  def make: ZIO[SttpClient, Nothing, AWSHttpClient] = {
      for{
          client <- ZIO.access[SttpClient](_.get)
      } yield
          AWSHttpClient(
              "http://localhost:8080",
            "ws://localhost:8080",
                 client,
          )
  }


  def clientLayer: ZLayer[SttpClient, Nothing, Has[HttpClient.Service]] =
    ZLayer.fromEffect(make)


  trait Service{
    def postIn[In](uriS: String, in: String)(implicit encoder: HttpClient.StringEncoder[In]): Task[Unit]
    def get[Out](uriS: String)(implicit decoder: HttpClient.StringDecoder[Out]): Task[String]
  }
  case class AWSHttpClient(uriHttpFront: String, 
                      uriWsFront: String,
                      backend: SttpBackend[Task, Stream[Throwable, Byte], WebSocketHandler],
                      ) extends ClientImp

  trait ClientImp extends Service {
      def uriHttpFront: String // front part of uri because we use relative uris here. Can be ""
      def uriWsFront: String   // front part of uri for web sockets

      def backend: SttpBackend[Task, Stream[Throwable, Byte], WebSocketHandler]


      def uriHttpOf(s: String): Uri = {
        val combined = s"$uriHttpFront/$s"
        //println(s"created combined url of $combined")
        uri"$combined"
      }


    def checkOk(response: Response[Either[String, String]]): Task[Unit] =
      response.body match {
        case Left(err) => {
          println(s"got response err $err")
          IO.fail(new Throwable( s"not good with ${response.code} and $err"))
        }
        case Right(ts) => {
          IO.unit
        }
      }

      override def postIn[T](uriS: String, in: String)(implicit encoder: HttpClient.StringEncoder[T]): Task[Unit] = {
        println(uriHttpOf(uriS))
        val request = basicRequest
          .post(uriHttpOf(uriS))
          .response(asString)
          .contentType(encoder.mediaType)
          .body(encoder.encode(in))
        for {
          resp <- backend.send(request)
          out  <- checkOk(resp)
        } yield out
      }

      def checkStringResponse[T](response: Response[Either[String, String]])(implicit decoder: HttpClient.StringDecoder[T]): Task[String] =
      response.body match {
        case Left(err) => {
          IO.fail(new Throwable( s"not good with ${response.code} and $err"))
        }
        case Right(ts) =>
          if (ts.toLowerCase.contains("exception")) {
            println(s"string response not ok $ts")
            IO.fail(new Throwable( s"not good with ${StatusCode.BadRequest} and $ts"))
          } else {
            decoder.decode(ts)
          }
      }

      override def get[T](uriS: String)(implicit decoder: HttpClient.StringDecoder[T]): Task[String] = {
        val request = basicRequest
          .get(uriHttpOf(uriS))
          .response(asString)
          .acceptEncoding(decoder.accepts)
        for {
          resp <- backend.send(request)
          out  <- checkStringResponse(resp)
        } yield out
      }

    
    
  }
}