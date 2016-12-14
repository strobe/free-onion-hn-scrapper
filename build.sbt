mainClass in (Compile, run) := Some("default.freeOnionHNScrapper.MainApp")

// H2 web console
val h2ConsoleTask = taskKey[Unit]("Started H2 Web Console")
h2ConsoleTask := (runMain in Compile).toTask(" default.freeOnionHNScrapper.H2ConsoleApp").value

lazy val freeOnionHNScrapper = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

libraryDependencies ++= Vector(
  Library.scalaXml,
  Library.scalaTest % "test",
  Library.cats,
  Library.scalajHttp,
  Library.monixEval,
  Library.monixCats,
  Library.freek,
  Library.tagsoup,
  Library.jsoup,
  Library.scalalikejdbc,
  Library.scalalikejdbcConfig,
  Library.h2,
  Library.logback
)

initialCommands := """|import default.freeOnionHNScrapper._
                      |""".stripMargin

// Kind-Projector //

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

// if your project uses both 2.10 and polymorphic lambdas
libraryDependencies ++= (scalaBinaryVersion.value match {
  case "2.10" =>
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) :: Nil
  case _ =>
    Nil
})

// resolver for Freek lib
resolvers += Resolver.bintrayRepo("projectseptemberinc", "maven")

// sbt-native-packager
enablePlugins(JavaAppPackaging)
