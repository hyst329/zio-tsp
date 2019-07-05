package nettest

import org.specs2._
import zio.{ Chunk, DefaultRuntime, UIO, ZIO }
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

import ParquetPkg._
import ParquetReader._

class NetSpec extends Specification with DefaultRuntime {

  // number of partitions
  val partNumber = 1

  val cfg = ConnectionConfig(
    server = buildVirtualServer,
    client = "client0",
    group = "group0",
    topic = "testTopic"
  )

  def buildVirtualServer: String = {

    val kafka           = EmbeddedKafka.start()
    val bootstrapServer = s"localhost:${kafka.config.kafkaPort}"
    bootstrapServer
  }

  def is = s2"""

  TSP Network should      
    hello     $hello

    """

  def hello = {

    val path = "/tmp/hello.pq"

    val rows =
      for {
        frame <- Reader.getFrame(path)
        size  = frame.getRowSize
        data  <- Reader.getRows(frame)
      } yield (size, data)

    // rows.dd
    unsafeRun(rows.map(println))

    true must_== true
  }

}
