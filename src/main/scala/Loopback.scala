package loop

import kafkaconsumer._

object Main extends App {

  val consumer = KafkaConsumer.settings("", "", "")

}
