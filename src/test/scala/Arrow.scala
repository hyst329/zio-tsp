//package nettest
//
//import org.specs2._
//
//// import java.io.{ ByteArrayInputStream }
//import java.util.Collections
//import java.util.Arrays.asList
//
//import zio.{ Chunk, DefaultRuntime }
//
//import org.apache.kafka.common.serialization.Serdes
//import net.manub.embeddedkafka.{ EmbeddedKafka }
//import zio.kafka.client.KafkaTestUtils.{ pollNtimes }
//import zio.kafka.client.{ Consumer, Subscription }
//import KafkaPkg._
//import KafkaTypes._
//import kafkaConsumer.KafkaConsumer.{ settings }
//
//import org.apache.arrow.memory.RootAllocator
//// import org.apache.arrow.vector.ipc.{ ArrowStreamReader }
//import org.apache.arrow.vector.{ IntVector, VectorSchemaRoot }
//import org.apache.arrow.vector.types.pojo.{ ArrowType, Field, FieldType, Schema }
//
//// import zio.serdes.Serdes._
//import ArrowPkg._
//
//class ArrowSpec extends Specification with DefaultRuntime {
//
//  val allocator = new RootAllocator(Integer.MAX_VALUE)
//
//  def is = s2"""
//
//  TSP Arrow should
//    display parquet file contents
//
//    consume parquet from prod
//    consume arrow from prod                 $testConsumeArrow
//    produce and consume arrow locally
//    killall                                 $killall
//
//    """
//  def testConsumeParquet = {
//
//    val slvCfg = SlaveConfig(
//      server = "37.228.115.243:9092",
//      client = "client5",
//      group = "group5",
//      topic = "parquet_small"
//    )
//
//    val exp = Array(1, 2, 3)
//
//    val subscription = Subscription.Topics(Set(slvCfg.topic))
//    val cons         = Consumer.make[String, BArr](settings(slvCfg))(Serdes.String, Serdes.ByteArray)
//
//    unsafeRun(
//      cons.use { r =>
//        for {
//          _     <- r.subscribe(subscription)
//          batch <- pollNtimes(5, r)
//          arr   = batch.map(_.value)
//          _     <- r.unsubscribe
//        } yield arr === exp
//
//      }
//    )
//  }
//
//  def testConsumeArrow = {
//
//    val slvCfg = SlaveConfig(
//      server = "37.228.115.243:9092",
//      client = "client5",
//      group = "group5",
//      topic = "batch_record_small_stream_writer"
//    )
//
//    val subscription = Subscription.Topics(Set(slvCfg.topic))
//    val cons         = Consumer.make[String, BArr](settings(slvCfg))(Serdes.String, Serdes.ByteArray)
//
//    val globalSchema = testSchema
//    val root         = simpleRoot(globalSchema)
//    root.getFieldVectors.get(0).allocateNew
//
//    unsafeRun(
//      cons.use { r =>
//        for {
//          _         <- r.subscribe(subscription)
//          batch     <- pollNtimes(5, r)
//          _         <- r.unsubscribe
//          arr       = batch.map(_.value)
//          reader    = deserialize(arr)
//          schema    = reader.map(r => r.getVectorSchemaRoot.getSchema)
//          empty     = reader.map(r => r.loadNextBatch)
//          bytesRead = reader.map(r => r.bytesRead)
//          rowCount  = reader.map(r => r.getVectorSchemaRoot.getRowCount)
//          _         = println(schema)
//          _         = println(rowCount)
//          _         = println(bytesRead)
//
//        } yield empty === Chunk(true)
//      }
//    )
//
//  }
//
//  // def testProduceAndConsumeArrow = {
//
//  //   val netCfg = NetConfig(
//  //     kafkaPort = 9003,
//  //     zooPort = 6001
//  //   )
//
//  //   val slvCfg = SlaveConfig(
//  //     server = s"localhost:${netCfg.kafkaPort}",
//  //     client = "client1",
//  //     group = "group1",
//  //     topic = "BArrTopic"
//  //   )
//
//  //   val cfg = EmbeddedKafkaConfig(kafkaPort = netCfg.kafkaPort, zooKeeperPort = netCfg.zooPort)
//  //   // EmbeddedKafka.start()(cfg)
//
//  //   // val data: BArr   = Array(1, 2, 3)
//  //   // val data   = Chunk(1, 2, 3)
//  //   // val stream = scatter(data)
//  //   val root = simpleRoot(testSchema)
//  //   root.getFieldVectors.get(0).allocateNew
//  //   // val vec: TinyIntVector = root.getFieldVectors.get(0).asInstanceOf[TinyIntVector]
//
//  //   val out = new ByteArrayOutputStream
//
//  //   val writer = new ArrowStreamWriter(root, null, out)
//  //   writer.close
//
//  //   out.size must be > 0
//
//  // }
//
//  // def serialize(din: Chunk[Int]) = {
//  //   val stream = scatter(din)
//  //   // val writer = new ArrowStreamWriter (allocator, )
//
//  // }
//
//  // Test helpers
//
//  def testSchema = {
//    val schema = new Schema(
//      asList(new Field("testField", FieldType.nullable(new ArrowType.Int(8, true)), Collections.emptyList()))
//    )
//    schema
//  }
//
//  def simpleSchema(vec: IntVector) =
//    new Schema(Collections.singletonList(vec.getField), null)
//
//  def simpleRoot(schema: Schema): VectorSchemaRoot =
//    VectorSchemaRoot.create(schema, allocator)
//
//  def killall = {
//    EmbeddedKafka.stop
//    true === true
//  }
//}
