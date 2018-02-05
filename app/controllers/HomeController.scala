package controllers

import javax.inject._

import actors.ClusterInteractingActor
import actors.ClusterInteractingActor.ClusterStateResponse
import akka.actor.{ActorSystem, Address}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.CurrentClusterState
import play.api._
import play.api.mvc._
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json.{JsArray, JsValue, Json, Writes}

import scala.concurrent.duration._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(system: ActorSystem, cc: ControllerComponents) extends AbstractController(cc) {

  implicit val timeout: Timeout = 3.seconds
  implicit val ec = system.dispatcher

  val node = Cluster(system)
  node.join(node.selfMember.address)

  val clusterActor = system.actorOf(ClusterInteractingActor.props, "hello-actor")

  implicit val addressWrites = Writes[Address](o =>
    Json.obj(
      "host" -> o.host,
      "port" -> o.port
    ))

  implicit val memberWrites = Writes[Member](m =>
    Json.obj(
      "address" -> m.address
    ))

  implicit val stateWrites = Writes[CurrentClusterState](o =>
    Json.obj(
      "leader" -> o.leader,
      "members" -> Json.toJson(o.members.toList)
    ))

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action.async { implicit request: Request[AnyContent] =>
    (clusterActor ? ClusterInteractingActor.RequestClusterState)
      .mapTo[ClusterInteractingActor.ClusterStateResponse]
      .map { response =>
        Ok(Json.toJson(response.state))
      }
  }
}
