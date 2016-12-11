import sbt._

// format: off

object Version {
  final val Scala     = "2.12.0"
  final val ScalaTest = "3.0.1"
  final val Cats      = "0.8.1"
//  final val Httpc     = "0.3.2"
  //final val ScalaScrapper = "1.1.0"
  final val scalajHttp = "2.3.0"
  final val Monix     = "2.1.0"
  final val Freek     = "0.6.5"
  final val ScalaLikeJDBC = "2.5.0"
  final val ScalaLikeJDBCConfig = "2.5.0"
  final val H2 = "1.4.193"
  final val LogBack = "1.1.7"
  // html
  final val Tagsoup   = "1.2.1"
  final val Jsoup     = "1.6.1"
}

object Library {
  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
  val cats = "org.typelevel" %% "cats" % Version.Cats
  val scalajHttp = "org.scalaj" %% "scalaj-http" % Version.scalajHttp
//  val httpc = "io.github.amrhassan" %% "httpc" % Version.Httpc
  //val scalaScrapper = "net.ruippeixotog" %% "scala-scraper" % Version.ScalaScrapper
  val monixEval = "io.monix" %% "monix-eval" % Version.Monix
  val monixCats = "io.monix" %% "monix-cats" % Version.Monix
  val tagsoup = "org.ccil.cowan.tagsoup" % "tagsoup" % Version.Tagsoup
  val jsoup = "org.jsoup" % "jsoup" % Version.Jsoup
  val scalalikejdbc = "org.scalikejdbc" %% "scalikejdbc" % Version.ScalaLikeJDBC
  val scalalikejdbcConfig = "org.scalikejdbc" %% "scalikejdbc-config"  % Version.ScalaLikeJDBCConfig
  val h2 = "com.h2database"  %  "h2" % Version.H2
  val logback = "ch.qos.logback"  %  "logback-classic"   % Version.LogBack
  val freek = "com.projectseptember" %% "freek" % Version.Freek exclude("org.typelevel", "cats-free_2.12.0-RC2") // TODO: remove it then freek will start using release version of cats for 2.12
}
