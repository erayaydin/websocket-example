import akka.actor.{Actor, ActorRef, PoisonPill}
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write

class ChatRoomActor extends Actor {
  import ChatRoomActor._

  override def receive: Receive = active(Map.empty[String, ActorRef])

  def active(users: Map[String, ActorRef]): Receive = {
    case UserJoined(name, actorRef) if users.contains(name) =>
      actorRef ! PoisonPill

    case UserJoined(name, actorRef) =>
      val newUserList = users + (name -> actorRef)
      context.become(active(newUserList))
      println(s"$name joined to the chatroom")
      actorRef ! write(Response("users", UserList(newUserList.keys)))(DefaultFormats)
      broadcast(users, write(Response("join", UserJoin(name)))(DefaultFormats))

    case UserLeft(name) =>
      context.become(active(users - name))
      println(s"$name left the chatroom")
      broadcast(users, write(Response("leave", UserLeave(name)))(DefaultFormats))

    case UserSaid(name, msg) =>
      println(s"$name: $msg")
      broadcast(users, write(Response("message", Message(s"$name: $msg")))(DefaultFormats))
  }
}

object ChatRoomActor {
  sealed trait UserEvent
  case class UserJoined(name: String, userActor: ActorRef) extends UserEvent
  case class UserLeft(name: String) extends UserEvent
  case class UserSaid(name: String, msg: String) extends UserEvent

  sealed trait ResponseData
  case class UserList(users: Iterable[String]) extends ResponseData
  case class UserJoin(user: String) extends ResponseData
  case class UserLeave(user: String) extends ResponseData
  case class Message(message: String) extends ResponseData
  case class Response(event: String, data: ResponseData)

  def broadcast(users: Map[String, ActorRef], msg: String): Unit =
    users.values.foreach(_ ! msg)
}
