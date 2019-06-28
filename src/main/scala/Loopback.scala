package loop

import kafkaconsumer._

object Main extends App {

  val server   = "0.0.0.0"
  val clientID = "client0"
  val groupID  = "group0"
  val topic    = "topic0"

  val res0 = KafkaConsumer.subscribe(server, clientID, groupID, topic)
  val res1 = res0 *> KafkaConsumer.unsubscribe(server, clientID, groupID)
  val data = KafkaConsumer.poll(server, clientID, groupID)

  /* val data =
   run(groupID, clientID) { consumer =>
    for {
      _       <- consumer.subscribe(Subscription.Topics(Set("topic1"))).either
      outcome <- consumer.unsubscribe.either
      _       <- ZIO.effect(outcome.isRight shouldBe true)
    } yield ()
  } */

}
