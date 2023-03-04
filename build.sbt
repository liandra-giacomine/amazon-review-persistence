val Http4sVersion          = "0.23.6"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"
val CirceVersion           = "0.14.3"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(inThisBuild(buildSettings))
  .settings(scalacSettings)
  .settings(
    organization := "com.example",
    name         := "amazon-review-persistance",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.10",
    libraryDependencies ++= Seq(
      "org.http4s"        %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"        %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"        %% "http4s-circe"        % Http4sVersion,
      "org.http4s"        %% "http4s-dsl"          % Http4sVersion,
      "io.circe"          %% "circe-generic"       % CirceVersion,
      "io.circe"          %% "circe-parser"        % CirceVersion,
      "org.mongodb.scala" %% "mongo-scala-driver"  % "4.8.0",
      "org.scalameta"     %% "munit"               % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic"     % LogbackVersion,
      "org.scalactic" %% "scalactic"           % "3.2.15",
      "org.scalatest" %% "scalatest"           % "3.2.15"               % Test,
      "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % Test
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )

lazy val buildSettings = Def.settings(scalafmtOnCompile := true)

lazy val scalacSettings = Def.settings(
  scalacOptions ~= { opts =>
    opts.filterNot(Set("-Xfatal-warnings"))
  }
)
