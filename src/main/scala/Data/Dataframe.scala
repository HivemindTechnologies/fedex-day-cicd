package Data

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.json4s.jackson.JsonMethods
import org.json4s.JsonAST

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import zio.{Task, ZIO}

object Dataframe{

  // combine two function together
  def combine(jv: String, path: Option[String], spark: SparkSession): ZIO[Any, Throwable, JsonAST.JValue] = {
    val parsed  = parseBody(JsonMethods.parse(jv))
    for {
      res <- path match {
        case None => filteDf(parsed, spark)
        case Some(value) => filteDf(parsed, spark, value)
      }
    } yield res
  }

  // parse the request body to a map
  def parseBody(jv: JsonAST.JValue): Map[String, Long] = {
    
    val toMap = jv.values.asInstanceOf[Map[String, Any]]

    val res = toMap.map{
      case (k, v) => {
        if (v.isInstanceOf[String]){
          val date = LocalDate.parse(v.asInstanceOf[String], DateTimeFormatter.ofPattern("dd.MM.yyyy"))
          (k, date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()/1000)
        }
        else (k, v.asInstanceOf[BigInt].toLong)
      }
    }
    res
  }

  // get the result(JValue) from the request body and data using spark
  def filteDf(request: Map[String, Long],
              spark: SparkSession,
      path: String = "src/main/scala/resources/video_game_reviews_example.json"): Task[JsonAST.JValue] = Task{
        val amazonDF = spark.read.json(path)
        // amazonDF.show(false)
        // create a temporary view using the DataFrame
        amazonDF.createOrReplaceTempView("product")
        // println(request("start"))
        // println(request("end"))
        val filterSql1 = s"""
                          |select  asin, AVG(overall)
                          |from 
                          |( select *, 
                          |       count(1) over (partition by asin) as occurs
                          |   from product
                          |) AS product
                          |where unixReviewTime between ${request("start")} and ${request("end")}
                          |  and
                          | occurs > ${request("min_number_reviews") - 1}
                          |group by asin
                          |order by AVG(overall) DESC
                          |limit ${request("limit")};""".stripMargin
        val duringTimeDf = spark.sql(filterSql1)
//         duringTimeDf.show()
//        println(JsonMethods.parse("[" + duringTimeDf.toJSON.collect().mkString(",") + "]"))

        println(JsonMethods.pretty(JsonMethods.parse("[" + duringTimeDf.toJSON.collect().mkString(",") + "]")))

        duringTimeDf.toJSON.write.json("src/main/scala/resources/outputs/result.json")
        JsonMethods.parse("[" + duringTimeDf.toJSON.collect().mkString(",") + "]")
    }
    
}