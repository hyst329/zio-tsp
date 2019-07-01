package kafkatest

import zio.{ UIO, ZIO }
import org.specs2._
import kafkaconsumer._

trait KafkaConfig {

  val cfg = ConnectionConfig(
    server = "localhost:9092",
    client = "client0",
    group = "group0",
    topic = "testTopic"
  )

}

class PollSpec extends Specification with KafkaConfig {
  def is = s2"""

 TSP Frontend should
    subscribe a topic $e1    
    poll and return a non-empty Chunk $e2
                                 """

  def e1 = {    
    val res: UIO[Boolean] =
      for {
        tmp <- KafkaConsumer.subscribe(cfg).either
        out = tmp.isRight
      } yield out

    res must_== UIO(true)

  }

  def e2 = {
    val res = KafkaConsumer.readBatch(cfg)
    res.isEmpty must_== false
  }

}
