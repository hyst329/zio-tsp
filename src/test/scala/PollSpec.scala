package kaftest

import org.specs2._
import zio.{ Chunk, DefaultRuntime, IO, UIO, ZIO }
//import zio.console.{ putStrLn }

import net.manub.embeddedkafka.EmbeddedKafka
import kafkaconsumer._
import kafkaconsumer.KafkaConsumer.KafkaData
import zio.kafka.client.KafkaTestUtils.{ produceMany }

class PollSpec extends Specification with DefaultRuntime {
  // val kafka = EmbeddedKafka.start()
  //EmbeddedKafka.publishStringMessageToKafka()

  type KafkaDataOld = List[(String, String)]

  // number of messages to produce
  val msgCount   = 2
  val partNumber = 1

  def genPortRange(start: Int, end: Int): Int =
    start + scala.util.Random.nextInt((end - start) + 1)

  def genData: KafkaDataOld =
    (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))

  def buildVirtualServer: String = {

    val kafka = EmbeddedKafka.start()
    // val port = genPortRange(9090, 9200)
    //val port = "6001"
    //println(s"Starting Kafka Server on port $port")
    //val bootstrapServer = s"localhost:$port"
    val bootstrapServer = s"localhost:${kafka.config.kafkaPort}"

    bootstrapServer
  }

  def is = s2"""

  TSP Kafka should
    produce and consume                         $t4
    shutdown all                                $shutdown  

    """
  // publish data to a topic                     $t0
  // poll and peek                               $t2

  // subscribe for a topic                       $t1
  // poll and read                               $t3

  def t0 = {

    val cfg = ConnectionConfig(
      server = buildVirtualServer,
      client = "client0",
      group = "group0",
      topic = "testTopic"
    )

    val res: IO[Throwable, Boolean] =
      //val res:UIO[Boolean] =
      for {
        _ <- KafkaConsumer.subscribe(cfg)
        //_ = println("subscriber done")
        _ <- ZIO.effect(EmbeddedKafka.createCustomTopic(cfg.topic, partitions = partNumber))
        //_ = println("create topic done")
        resp <- ZIO.effect(produceMany(cfg.topic, genData)).either
        //_ = println("producemany done")
      } yield (resp.isRight == true)

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

    //val data: Chunk[String] = KafkaConsumer.peekBatch(cfg)
    val data =
      for {
        _   <- KafkaConsumer.produce(cfg)
        out = KafkaConsumer.peekBatch(cfg)

      } yield out

    data must_== Chunk.fromIterable(genData)
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

  def t4() = {

    val cfg = ConnectionConfig(
      server = buildVirtualServer,
      client = "client0",
      group = "group0",
      topic = "testTopic"
    )

    val data: KafkaData = KafkaConsumer.produceAndConsume(cfg)

    // Validate output
    data.map { r =>
      (r.key, r.value)
    } must_== Chunk.fromIterable(genData)

  }

  def shutdown() = {
    EmbeddedKafka.stop()
    true must_== true
  }
}
