import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import java.util.concurrent.Executors

object ChatServer extends App {
  implicit val system: ActorSystem = ActorSystem("app")
  implicit val materializer: Materializer = Materializer.matFromSystem
  implicit val executionContext: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(16))

  private val chatroom = new ChatRoom()

  val webRoute: Route =
    pathEndOrSingleSlash {
      getFromResource("web/index.html")
    } ~
      getFromResourceDirectory("web")

  val apiRoute: Route =
    path( "api" / "chat") {
      get {
        parameters("name") { name =>
          handleWebSocketMessages(chatroom.websocketFlow(name))
        }
      }
    }

  Http().newServerAt("localhost", 5556).bind(apiRoute ~ webRoute).map { binding =>
    println(s"Server is running at ${binding.localAddress.getHostName}:${binding.localAddress.getPort}")
  }
}
