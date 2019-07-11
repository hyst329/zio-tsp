package nettest

import org.specs2._

import zio.{ Chunk, DefaultRuntime, UIO }
// import zio.console.{ putStrLn }

import net.manub.embeddedkafka.EmbeddedKafka
//import kafkaconsumer._
import kafkaconsumer.KafkaConsumer._
import zio.kafka.client.KafkaTestUtils.{ produceMany }
import KafkaPkg._
import KafkaTypes._
import zio.kafka.client.{ Consumer, Subscription }
import zio.kafka.client.KafkaTestUtils.{ pollNtimes, produceChunk, produceMany }
import zio.kafka.client._

import org.apache.kafka.common.serialization.Serdes

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
    display parquet file contents     
    publish Strings   to Kafka        
    publish Byte Arr  to Kafka        $pubArr

    """

  def disp = {

    unsafeRun(rows.map(println))
    true must_== true
  }

  def pubString = {
    type WorkType = BlockingTask[Chunk[(String, String)]]

    val subscription = Subscription.Topics(Set(cfg.topic))

    val cons = Consumer.make[String, String](settings(cfg))

    val res: WorkType = cons.use { r =>
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

  def pubArr = {

    val data: BArr = Array(1, 2, 3)

    val subscription = Subscription.Topics(Set(cfg.topic))

    val cons = Consumer.make[String, BArr](settings(cfg))(Serdes.String, Serdes.ByteArray)

    unsafeRun(
      cons.use { r =>
        for {
          _       <- r.subscribe(subscription)
          _       <- produceChunk(cfg.topic, data)
          batch   <- pollNtimes(5, r)
          arr     = batch.map(_.value)
          compare = arr.map(p => BArrEq.eqv(p, data))

        } yield compare must_== Chunk(true)

      }
    )
  }
}
