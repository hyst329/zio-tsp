package nettest

import org.specs2._

import zio.{ DefaultRuntime }
// import zio.console.{ putStrLn }

import net.manub.embeddedkafka.{ EmbeddedKafka }
import kafkaconsumer.KafkaConsumer._
import zio.kafka.client.KafkaTestUtils.{ pollNtimes }
import KafkaPkg._
import KafkaTypes._
import zio.kafka.client.{ Consumer, Subscription }
import zio.kafka.client._

import org.apache.kafka.common.serialization.Serdes

class ArrowSpec extends Specification with DefaultRuntime {

  val exp = Array(1, 2, 3)

  def is = s2"""

  TSP Arrow should      
    display parquet file contents     

    consume parquet from prod       
    consume arrow from prod         $prodArrowTest

    killall                         $killall

    """
  def prodParquetTest = {

    val slvCfg = SlaveConfig(
      server = "37.228.115.243:9092",
      client = "client5",
      group = "group5",
      topic = "parquet_small"
    )

    val subscription = Subscription.Topics(Set(slvCfg.topic))
    val cons         = Consumer.make[String, BArr](settings(slvCfg))(Serdes.String, Serdes.ByteArray)

    unsafeRun(
      cons.use { r =>
        for {
          _     <- r.subscribe(subscription)
          batch <- pollNtimes(5, r)
          arr   = batch.map(_.value)
          _     <- r.unsubscribe
        } yield arr === exp

      }
    )
  }

  def prodArrowTest = {

    val slvCfg = SlaveConfig(
      server = "37.228.115.243:9092",
      client = "client5",
      group = "group5",
      topic = "batch_record_small"
      // topic = "table_small"
    )

    val subscription = Subscription.Topics(Set(slvCfg.topic))
    val cons         = Consumer.make[String, BArr](settings(slvCfg))(Serdes.String, Serdes.ByteArray)

    unsafeRun(
      cons.use { r =>
        for {
          _     <- r.subscribe(subscription)
          batch <- pollNtimes(5, r)
          arr   = batch.map(_.value)
          _     <- r.unsubscribe
        } yield arr === exp

      }
    )

  }

  def killall() = {
    EmbeddedKafka.stop
    true === true
  }
}
