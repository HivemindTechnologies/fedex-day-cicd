import zio.test.DefaultRunnableSpec
import Http.{HttpClient, HttpService, Utils}
import zio.test._
import TestAspect._
import zio.UIO
import zio.ZIO
import Http.HttpClient._
import uzhttp.Request
import zio.magic.ZSpecProvideMagicOps
import Http.EndPoint._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import DB.ScalaJdbcConnectSelect._

object clientTest extends DefaultRunnableSpec  {
  override def spec = sequential(suite("test client/server")(
    serverIsUp,
    //  testPost,
  //  testGet,
    // testDB,
  ).injectCustomShared(
     HttpService.serverLayer,
    HttpClient.clientLayer,
    AsyncHttpClientZioBackend.layer()).mapError{ e =>
    println(s"initialisation error $e");
    TestFailure.fail(e)
  })



  def testPost: ZSpec[HttpClient, Throwable] = testM("test post"){
    for{
        _ <- ZIO.accessM[HttpClient](_.get.postIn("product", "product"))
        p <- ZIO.accessM[HttpClient](_.get.get("product"))
        _ <- UIO(println(p))
    } yield assertCompletes
  }

  def testGet = testM("test get"){
    for {
      p <- ZIO.accessM[HttpClient](_.get.get("product"))
      _ <- UIO(println(p))
    } yield assertCompletes
  }

  val serverIsUp = testM("server is up") {
    for {
      _ <- Utils.serverUp
      _ <- UIO(println(s"server is up"))
    } yield assertCompletes
  }

  val testDB = test("db test"){
    simpleTest
    assertCompletes
  }
}
