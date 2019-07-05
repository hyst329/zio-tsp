package kaftest

import org.specs2._
import zio.{ Chunk, DefaultRuntime, ZIO }
//import zio.console.{ putStrLn }

import net.manub.embeddedkafka.EmbeddedKafka
//import kafkaconsumer._
import kafkaconsumer.KafkaConsumer._
import zio.kafka.client.KafkaTestUtils.{ produceMany }
import KafkaPkg._
import KafkaTypes._
import zio.kafka.client.{ Consumer, Subscription }
import zio.kafka.client.KafkaTestUtils.{ pollNtimes, produceMany }
import zio.kafka.client._

class KafkaSpec extends Specification with DefaultRuntime {

  val servers = List("localhost:9091", "localhost:9092", "localhost:9093", "localhost:9094")

  /* val kafka = EmbeddedKafka.start()

  val cfg = ConnectionConfig(
    server = servers,
    client = "client0",
    group = "group0",
    topic = "testTopic"
  ) */
  val cfg = ConnectionConfig(
    server = buildVirtualServer,
    client = "client0",
    group = "group0",
    topic = "testTopic"
  )

  // number of messages to produce
  val msgCount   = 2
  val partNumber = 1

  def genPortRange(start: Int, end: Int): Int =
    start + scala.util.Random.nextInt((end - start) + 1)

  def buildVirtualServer: String = {

    val kafka = EmbeddedKafka.start()
    //val port = genPortRange(9090, 9200)
    //println(s"Starting Kafka Server on port $port")
    //val bootstrapServer = s"localhost:$port"
    val bootstrapServer = s"localhost:${kafka.config.kafkaPort}"

    bootstrapServer
  }

  def is = s2"""

  TSP Kafka should
    subscribe, create a topic and unsubscribe           $subscr         
    publish and poll                                    $pubPoll
    

    """

  /* def subscr = {
    val cons = Consumer.make[String, String](settings(cfg))

    val res: IO[Throwable, Boolean] =
      //val res:UIO[Boolean] =
      for {
        cons <- cons.subscribe(cfg)
        _ <- ZIO.effect(EmbeddedKafka.createCustomTopic(cfg.topic, partitions = partNumber))
        resp <- cons.unsubscribe.either

      } yield (resp.isRight == true)

    unsafeRun(res) must_== true
  } */

  def pubPoll = {

    val subscription = Subscription.Topics(Set(cfg.topic))

    val cons = Consumer.make[String, String](settings(cfg))

    val res: BlockingTask[Chunk[(String, String)]] = cons.use { r =>
      for {

        _    <- r.subscribe(subscription)
        _    <- produceMany(cfg.topic, genDummyData)
        data <- pollNtimes(5, r)

      } yield data.map { r =>
        (r.key, r.value)
      }
    }

    unsafeRun(res) must_== Chunk.fromIterable(genDummyData)
  }

  def subscr = {

    val subscription = Subscription.Topics(Set(cfg.topic))

    val cons = Consumer.make[String, String](settings(cfg))

    val res: BlockingTask[Boolean] = cons.use { r =>
      for {

        _    <- r.subscribe(subscription)
        _    <- ZIO.effect(EmbeddedKafka.createCustomTopic(cfg.topic, partitions = partNumber))
        resp <- r.unsubscribe.either

      } yield (resp.isRight == true)

    }

    unsafeRun(res) must_== true
  }

}
