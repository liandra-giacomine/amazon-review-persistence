Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(inThisBuild(buildSettings))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
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
