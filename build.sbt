
// The simplest possible sbt build file is just one line:
inThisBuild(Seq(
  version := "1.0",
  isSnapshot := true,
  resolvers +=   "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += "oss" at  "https://oss.sonatype.org/content/groups/public/",
//  resolvers += "univalence" at "http://dl.bintray.com/univalence/univalence-jvm",
  scalaVersion := "2.13.4",
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.2" cross CrossVersion.full)
  
))
// That is, to create a valid sbt build, all you've got to do is define the
// version of Scala you'd like your project to use.

// ============================================================================

// Lines like the above defining `scalaVersion` are called "settings". Settings
// are key/value pairs. In the case of `scalaVersion`, the key is "scalaVersion"
// and the value is "2.13.3"

// It's possible to define many kinds of settings, such as:


// Note, it's not required for you to define these three settings. These are
// mostly only necessary if you intend to publish your library's binaries on a
// place like Sonatype.
name := "hello-world"
organization := "ch.epfl.scala"
enablePlugins(JavaAppPackaging, DockerPlugin)
// Want to use a published library in your project?
// You can define other libraries as dependencies in your build like this:
lazy val `zioVersion` = "1.0.4"
lazy val zio = "dev.zio" %% "zio" %  zioVersion
lazy val `zio-test` = "dev.zio" %% "zio-test" %  zioVersion % Test
lazy val `zio-streams` = "dev.zio" %% "zio-streams" %  zioVersion
lazy val `zio-test-not-test` = "dev.zio" %% "zio-test" % `zioVersion`
lazy val `zio-test-sbt` = "dev.zio" %% "zio-test-sbt" %  zioVersion % Test
val zioMagic = "io.github.kitlangton" %% "zio-magic" % "0.2.0"
lazy val scala_module = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
val uzhttp = "org.polynote" %% "uzhttp" % "0.2.6"
val cats = "org.typelevel" %% "cats-core" % "2.5.0"
val json4s   = "org.json4s" %% "json4s-jackson" % "3.6.9"
// val sttpVersion = "3.3.15"
//val sttp = "com.softwaremill.sttp.client3" %% "core" % "3.3.15"
//val sttpzio  = "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.3.15"
val sttpVersion = "2.2.9"
val sttp = "com.softwaremill.sttp.client" %% "core" % sttpVersion
val sttpzio  = "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % sttpVersion
val sttpziostreams  = "com.softwaremill.sttp.client" %% "async-http-client-backend-zio-streams" % sttpVersion

val postgres = "org.postgresql" % "postgresql" % "42.2.14"
val postgisJts = "net.postgis" % "postgis-jdbc-jtsparser" % "2.5.0"
val slick  = "com.typesafe.slick" %% "slick" % "3.3.3"
val slf4j = "org.slf4j" % "slf4j-nop" % "1.6.4"


libraryDependencies ++= Seq(
    zio,
    `zio-test`,
    `zio-streams`,
    `zio-test-not-test`,
    `zio-test-sbt`,
    zioMagic,
    scala_module,
    uzhttp,
    cats,
    json4s,
    sttp,
    sttpzio,
    postgres,
    postgisJts,
    slick,
    slf4j

)

// Here, `libraryDependencies` is a set of dependencies, and by using `+=`,
// we're adding the scala-parser-combinators dependency to the set of dependencies
// that sbt will go and fetch when it starts up.
// Now, in any Scala file, you can import classes, objects, etc., from
// scala-parser-combinators with a regular import.

// TIP: To find the "dependency" that you need to add to the
// `libraryDependencies` set, which in the above example looks like this:

// "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

// You can use Scaladex, an index of all known published Scala libraries. There,
// after you find the library you want, you can just copy/paste the dependency
// information that you need into your build file. For example, on the
// scala/scala-parser-combinators Scaladex page,
// https://index.scala-lang.org/scala/scala-parser-combinators, you can copy/paste
// the sbt dependency from the sbt box on the right-hand side of the screen.

// IMPORTANT NOTE: while build files look _kind of_ like regular Scala, it's
// important to note that syntax in *.sbt files doesn't always behave like
// regular Scala. For example, notice in this build file that it's not required
// to put our settings into an enclosing object or class. Always remember that
// sbt is a bit different, semantically, than vanilla Scala.

// ============================================================================

// Most moderately interesting Scala projects don't make use of the very simple
// build file style (called "bare style") used in this build.sbt file. Most
// intermediate Scala projects make use of so-called "multi-project" builds. A
// multi-project build makes it possible to have different folders which sbt can
// be configured differently for. That is, you may wish to have different
// dependencies or different testing frameworks defined for different parts of
// your codebase. Multi-project builds make this possible.

// Here's a quick glimpse of what a multi-project build looks like for this
// build, with only one "subproject" defined, called `root`:

// lazy val root = (project in file(".")).
//   settings(
//     inThisBuild(List(
//       organization := "ch.epfl.scala",
//       scalaVersion := "2.13.3"
//     )),
//     name := "hello-world"
//   )

// To learn more about multi-project builds, head over to the official sbt
// documentation at http://www.scala-sbt.org/documentation.html
