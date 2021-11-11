import zio.test._
import Assertion._
import Http.HttpService._
import Data.Dataframe._
import org.apache.spark.sql.SparkSession
import org.json4s.jackson.JsonMethods
import zio.UIO

object testDataframe extends DefaultRunnableSpec{
    override def spec: ZSpec[Environment,Failure] = suite("test dataframe")(
        testRun
    )
    val spark: SparkSession = SparkSession.builder()
      .master("local[3]")
      .appName("Webservice")
      .getOrCreate()
    
    val requestString ="""{
                        |    "start": "01.01.2010",
                        |    "end": "31.12.2020",
                        |    "limit": 2,
                        |    "min_number_reviews": 2
                        |}""".stripMargin

    def testRun = testM("test runnable"){
        val parsed = parseBody(JsonMethods.parse(requestString))
        for{
            res <- filteDf(parsed, spark)
            _ <- UIO(println(JsonMethods.pretty(res)))
        } yield
        assertCompletes
    }

    
}