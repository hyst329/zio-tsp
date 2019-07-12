package nettest

import org.specs2._

import zio.{ Chunk, DefaultRuntime, UIO }
// import zio.console.{ putStrLn }

import net.manub.embeddedkafka.{ EmbeddedKafka, EmbeddedKafkaConfig }
//import kafkaconsumer._
import kafkaconsumer.KafkaConsumer._
import zio.kafka.client.KafkaTestUtils.{ pollNtimes, produceChunk, produceMany }
import KafkaPkg._
import KafkaTypes._
import zio.kafka.client.{ Consumer, Subscription }
import zio.kafka.client._

import org.apache.kafka.common.serialization.Serdes

import ParquetPkg._
import ParquetReader._

class NetSpec extends Specification with DefaultRuntime {

  // Read parquet data
  val path = "/tmp/hello.pq"

  val rows: UIO[(Int, TypeData)] =
    for {
      frame <- Reader.getFrame(path)
      size  = frame.getRowSize
      data  <- Reader.getRows(frame)
    } yield (size, data)

  // number of partitions
  val partNumber = 1

  def is = s2"""

  TSP Network should      
    display parquet file contents     $disp
    publish Strings   to Kafka        $pubString
    publish Byte Arr  to Kafka        $pubArr

    killall                           $killall

    """

  def disp = {
    unsafeRun(rows.map(println))
    true must_== true
  }

  def pubString = {

    type WorkType = BlockingTask[Chunk[(String, String)]]

    val netCfg = NetConfig(
      kafkaPort = 9002,
      zooPort = 6000
    )

    val slvCfg = SlaveConfig(
      server = s"localhost:${netCfg.kafkaPort}",
      client = "client0",
      group = "group0",
      topic = "stringTopic"
    )

    val cfg = EmbeddedKafkaConfig(kafkaPort = netCfg.kafkaPort, zooKeeperPort = netCfg.zooPort)
    EmbeddedKafka.start()(cfg)

    val subscription = Subscription.Topics(Set(slvCfg.topic))

    val cons = Consumer.make[String, String](settings(slvCfg))

    val res: WorkType = cons.use { r =>
      for {

        _    <- r.subscribe(subscription)
        _    <- produceMany(netCfg, slvCfg.topic, genDummyData)
        data <- pollNtimes(5, r)
        _    = EmbeddedKafka.stop

      } yield data.map { r =>
        (r.key, r.value)
      }
    }

    unsafeRun(res) must_== Chunk.fromIterable(genDummyData)
  }

  def pubArr = {

    val netCfg = NetConfig(
      kafkaPort = 9003,
      zooPort = 6001
    )

    val slvCfg = SlaveConfig(
      server = s"localhost:${netCfg.kafkaPort}",
      client = "client1",
      group = "group1",
      topic = "BArrTopic"
    )

    val cfg = EmbeddedKafkaConfig(kafkaPort = netCfg.kafkaPort, zooKeeperPort = netCfg.zooPort)
    EmbeddedKafka.start()(cfg)

    val data: BArr = Array(1, 2, 3)

    val subscription = Subscription.Topics(Set(slvCfg.topic))

    val cons = Consumer.make[String, BArr](settings(slvCfg))(Serdes.String, Serdes.ByteArray)

    unsafeRun(
      cons.use { r =>
        for {
          _       <- r.subscribe(subscription)
          _       <- produceChunk(netCfg, slvCfg.topic, data)
          batch   <- pollNtimes(5, r)
          _       = EmbeddedKafka.stop
          arr     = batch.map(_.value)
          compare = arr.map(p => BArrEq.eqv(p, data))

        } yield compare must_== Chunk(true)

      }
    )
  }

  def killall() = {
    EmbeddedKafka.stop
    true must_== true
  }
}
