package DB


import java.sql.DriverManager
import java.sql.Connection

/**
 * A Scala JDBC connection example by Alvin Alexander,
 * https://alvinalexander.com
 */
object ScalaJdbcConnectSelect {

  def simpleTest =  {
    // connect to the database named "mysql" on the localhost
    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql://postgres:5432/postgres"
    val username = "postgres"
    val password = "changeme"

    // there's probably a better way to do this
    var connection:Connection = null

    try {
      // make the connection
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      // create the statement, and run the select query
      val statement = connection.createStatement()
      val resultSet = statement.executeQuery("SELECT id, username FROM users")
      while ( resultSet.next() ) {
        val host = resultSet.getString("id")
        val user = resultSet.getString("username")
        println("host, user = " + host + ", " + user)
      }
    } catch {
      case e: Throwable => e.printStackTrace
    }
    connection.close()
  }

}
