package loop

import zio.{ App, IO }
import kafkaconsumer._

object Main extends App {

  def run(args: List[String]) =
    prog.fold(_ => 1, _ => 0)

  // Effectful program
  val prog = IO {

    val cfg = ConnectionSetup(
      server = "localhost:9092",
      client = "client0",
      group = "group0",
      topic = "testTopic"
    )

    val res = KafkaConsumer.readBatch(cfg).map(println)

  }
}
