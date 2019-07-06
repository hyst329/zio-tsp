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

  val path = "/tmp/hello.pq"

  val rows: UIO[(Int, TypeData)] =
    for {
      frame <- Reader.getFrame(path)
      size  = frame.getRowSize
      data  <- Reader.getRows(frame)
    } yield (size, data)

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
    display parquet file contents     $disp
    publish parquet file to Kafka     $pub

    """

  def disp = {

    unsafeRun(rows.map(println))
    true must_== true
  }

  def pub = {

    val subscription = Subscription.Topics(Set(cfg.topic))

    val cons = Consumer.make[String, String](settings(cfg))

    val res: BlockingTask[Chunk[(String, String)]] = cons.use { r =>
      for {

        _    <- r.subscribe(subscription)
        _    <- produceMany[List, (String, String)](cfg.topic, genDummyData)
        data <- pollNtimes(5, r)

      } yield data.map { r =>
        (r.key, r.value)
      }
    }

    unsafeRun(res) must_== Chunk.fromIterable(genDummyData)

    //true must_== true
  }

}
