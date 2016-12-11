import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.GitPlugin.autoImport._
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import de.heikoseeberger.sbtheader.license._
import org.scalafmt.sbt.ScalaFmtPlugin
import org.scalafmt.sbt.ScalaFmtPlugin.autoImport._
import sbt._
import sbt.plugins.JvmPlugin
import sbt.Keys._

// format: off

object Build extends AutoPlugin {

  override def requires =
    JvmPlugin && HeaderPlugin && GitPlugin && ScalaFmtPlugin

  override def trigger = allRequirements

  override def projectSettings =
    reformatOnCompileSettings ++
    Vector(
           //scalaOrganization in ThisBuild := "org.typelevel",
           licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
           mappings.in(Compile, packageBin) += baseDirectory.in(ThisBuild).value / "LICENSE" -> "LICENSE",
           scalaVersion := Version.Scala,
//           crossScalaVersions := Vector(scalaVersion.value),
           scalacOptions ++= Vector(
             "-unchecked",
             "-deprecation",
             "-language:_",
             "-target:jvm-1.8",
             "-encoding", "UTF-8",
             "-feature",
             "-language:existentials",
             "-language:higherKinds",
             "-language:implicitConversions",
             "-Xfatal-warnings",
             "-Xlint",
             "-Yno-adapted-args",
             "-Ywarn-dead-code",
             "-Ywarn-numeric-widen",
             "-Ywarn-value-discard",
             "-Xfuture",
             "-Ypartial-unification" // enable fix for SI-2712 (typelevel only)
//             "-Yliteral-types"        // enable SIP-23 implementation (typelevel only)
           ),
           unmanagedSourceDirectories.in(Compile) := Vector(scalaSource.in(Compile).value),
           unmanagedSourceDirectories.in(Test) := Vector(scalaSource.in(Test).value),

           resolvers ++= Seq(
             Resolver.sonatypeRepo("releases"),
             Resolver.sonatypeRepo("snapshots")
           ),

           // scalafmt settings
           formatSbtFiles := false,
           scalafmtConfig := Some(baseDirectory.in(ThisBuild).value / ".scalafmt.conf"),
           ivyScala := ivyScala.value.map(_.copy(overrideScalaVersion = sbtPlugin.value)), // TODO Remove once this workaround no longer needed (https://github.com/sbt/sbt/issues/2786)!

           // Git settings
           git.useGitDescribe := true,

           // Header settings
           headers := Map("scala" -> Apache2_0("2016", "Evgeniy Tokarev"))
    )
}
