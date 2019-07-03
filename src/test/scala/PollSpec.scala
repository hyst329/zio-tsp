package kaftest

import org.specs2._
import zio.{ DefaultRuntime, UIO, ZIO }
//import zio.console.{ putStrLn }

import net.manub.embeddedkafka.EmbeddedKafka
import kafkaconsumer._
import zio.kafka.client.KafkaTestUtils.{ produceMany }

class PollSpec extends Specification with DefaultRuntime {
  EmbeddedKafka.start()

  def genPortRange(start: Int, end: Int) =
    start + scala.util.Random.nextInt((end - start) + 1)

  def buildVirtualServer: String = {

    val port = genPortRange(9090, 9200)
    println(s"Starting Kafka Server on port $port")
    val bootstrapServer = s"localhost:$port"
    bootstrapServer
  }

  // number of messages to produce
  val msgCount   = 2
  val partNumber = 1

  def is = s2"""

  TSP Kafka should
    subscribe for a topic                       $t1    
    poll and peek                               $t2  
    poll and read                               $t3
    shutdown all                                $shutdown  
    """

  //publish data to a topic                     $t0
  //produce, poll and peek                      $t4

  def t0 = {

    val cfg = ConnectionConfig(
      server = buildVirtualServer,
      client = "client0",
      group = "group0",
      topic = "testTopic"
    )

    //val res:IO[Throwable, Boolean] =
    val res =
      for {
        _    <- KafkaConsumer.subscribe(cfg)
        _    <- ZIO.effect(EmbeddedKafka.createCustomTopic(cfg.topic, partitions = partNumber))
        resp <- produceMany(cfg.topic, (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))).either
      } yield (resp.isRight)

    unsafeRun(res) must_== true

  }

  def t1 = {

    val cfg = ConnectionConfig(
      server = buildVirtualServer,
      client = "client0",
      group = "group0",
      topic = "testTopic"
    )

    val res: UIO[Boolean] =
      for {
        tmp <- KafkaConsumer.subscribe(cfg).either
        out = tmp.isRight
      } yield out

    unsafeRun(res) must_== true

  }

  def t2 = {

    val cfg = ConnectionConfig(
      server = buildVirtualServer,
      client = "client0",
      group = "group0",
      topic = "testTopic"
    )

    val res = KafkaConsumer.peekBatch(cfg)

    res.isEmpty must_== false
  }

  def t3 = {

    val cfg = ConnectionConfig(
      server = buildVirtualServer,
      client = "client0",
      group = "group0",
      topic = "testTopic"
    )

    val res = KafkaConsumer.readBatch(cfg)

    res.isEmpty must_== false
  }

  def shutdown() = {
    EmbeddedKafka.stop()
    true must_== true
  }
}
