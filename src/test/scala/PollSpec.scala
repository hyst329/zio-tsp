package kaftest

import zio.{ DefaultRuntime, UIO }
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

  val rt = new DefaultRuntime {}

  def is = s2"""

 TSP Frontend should
    subscribe for a topic $t1    
    poll and peek a non-empty Chunk from Kafka $t2
    poll and read a non-empty Chunk from Kafka $t3
    
    """

  def t1 = {
    val res: UIO[Boolean] =
      for {
        tmp <- KafkaConsumer.subscribe(cfg).either
        out = tmp.isRight
      } yield out

    rt.unsafeRun(res) must_== true

  }

  def t2 = {
    val res = KafkaConsumer.peekBatch(cfg)
    res.isEmpty must_== false
  }

  def t3 = {
    val res = KafkaConsumer.readBatch(cfg)
    res.isEmpty must_== false
  }

}
