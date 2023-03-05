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
    libraryDependencies ++= Dependencies.compile ++ Dependencies.test,
    testFrameworks += new TestFramework("munit.Framework")
  )

lazy val buildSettings = Def.settings(scalafmtOnCompile := true)

lazy val scalacSettings = Def.settings(
  scalacOptions ~= { opts =>
    opts.filterNot(Set("-Xfatal-warnings"))
  }
)
