import ChatRoomActor.{UserJoined, UserLeft, UserSaid}
import akka.Done
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{CompletionStrategy, Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.reactivestreams.Publisher

class ChatRoom()(implicit system: ActorSystem, mat: Materializer) {
  private val roomActor = system.actorOf(Props(classOf[ChatRoomActor]))

  def websocketFlow(name: String): Flow[Message, Message, Any] = {
    val (actorRef: ActorRef, publisher: Publisher[TextMessage.Strict]) =
      Source.actorRef({
        case Done => CompletionStrategy.immediately
      }, PartialFunction.empty, 16, OverflowStrategy.fail)
        .map(TextMessage.Strict)
        .toMat(Sink.asPublisher(false))(Keep.both).run()

    roomActor ! UserJoined(name, actorRef)

    val sink: Sink[Message, Any] = Flow[Message]
      .map {
        case TextMessage.Strict(msg) =>
          roomActor ! UserSaid(name, msg)
      }
      .to(Sink.onComplete( _ =>
        roomActor ! UserLeft(name)
      ))

    Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))
  }
}
