val Http4sVersion     = "0.21.2"
val CirceVersion      = "0.13.0"
val Specs2Version     = "4.8.3"
val LogbackVersion    = "1.2.3"
val MUnitVersion      = "0.7.2"
val PureConfigVersion = "0.12.3"
val SquantsVersion    = "1.6.0"
val MockitoVersion    = "1.13.9"

val ScalaVersion = "2.13.3"

lazy val root = (project in file("."))
  .settings(
    organization := "de.fschueler",
    name := "fermentation-station",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server"         % Http4sVersion,
      "org.http4s"            %% "http4s-blaze-client"         % Http4sVersion,
      "org.http4s"            %% "http4s-circe"                % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"                  % Http4sVersion,
      "io.circe"              %% "circe-generic"               % CirceVersion,
      "io.circe"              %% "circe-literal"               % CirceVersion,
      "org.typelevel"         %% "squants"                     % SquantsVersion,
      "com.github.pureconfig" %% "pureconfig"                  % PureConfigVersion,
      "ch.qos.logback"        % "logback-classic"              % LogbackVersion,
      "org.scalameta"         %% "munit"                       % MUnitVersion % Test,
      "org.mockito"           % "mockito-scala_2.13" % MockitoVersion % Test
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )

testFrameworks += new TestFramework("munit.Framework")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings"
)
