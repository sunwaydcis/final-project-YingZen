scalaVersion := "3.8.4"

lazy val root = project.in(file("."))
  .settings(
    name := "Project_24007908",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    scalacOptions ++= Seq(
      "-release", "21",
      "-Wunused:all",
      "-Wconf:cat=deprecation:s,msg=.*Implicit parameters.*:s"
    ),
    fork := true
  )
