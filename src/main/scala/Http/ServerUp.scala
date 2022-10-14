package Http

import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.magic.ZioProvideMagicOps
import zio.{App, ExitCode, UIO}

object ServerUp extends App {
  override def run(args: List[String]) = {
    
    serverUp.injectCustom(
      HttpService.serverLayer,
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
    // _ <- ZIO.accessM[HttpClient](_.get.get("product"))
    // _ <- clock.sleep(5000.seconds)
  } yield ()
}

