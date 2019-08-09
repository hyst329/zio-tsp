// Temporary: Simple Hello World, just to have a main class in the project (for Docker image building)

package hello

import zio.App
import zio.console.{ putStrLn }

object Main extends App {

  def run(args: List[String]) =
    myAppLogic.fold(_ => 1, _ => 0)

  val myAppLogic =
    for {
      _ <- putStrLn("Hello World")
    } yield ()
}