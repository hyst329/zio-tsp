package loop

import zio.{ App }
import zio.console.{ putStrLn }
//import kafkaconsumer._

object Main extends App {

  def run(args: List[String]) =
    prog.fold(_ => 1, _ => 0)

  // Effectful program
  val prog = {

    putStrLn("Loopback")

    /* val cfg = ConnectionConfig(
      server = "localhost:9092",
      client = "client0",
      group = "group0",
      topic = "testTopic"
    )

    val res = KafkaConsumer.readBatch(cfg).map(println) */

  }
}
