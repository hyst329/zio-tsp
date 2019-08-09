addSbtPlugin("org.scalameta"             % "sbt-scalafmt" % "2.0.2")
addSbtPlugin("ch.epfl.scala"             % "sbt-bloop"    % "1.3.2")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.7")
//addSbtPlugin("io.spray"      % "sbt-revolver"      % "0.9.1")
addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1") // reqd for kafka

addSbtPlugin("com.typesafe.sbt"  % "sbt-git"             % "1.0.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager" % "1.3.25")
addSbtPlugin("com.eed3si9n"      % "sbt-assembly"        % "0.14.10")
addSbtPlugin("com.github.gseitz" % "sbt-release"         % "1.0.11")