/**
 * Copyright (C) 2009-2018 Lightbend Inc. <http://www.lightbend.com>
 */
package actors

import actors.ClusterInteractingActor.{ClusterStateResponse, RequestClusterState}
import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState

object ClusterInteractingActor {


  case object RequestClusterState
  case class ClusterStateResponse(state: CurrentClusterState)

  def props = Props(new ClusterInteractingActor)

}

class ClusterInteractingActor extends Actor {

  val cluster = Cluster(context.system)

  def receive = {
    case RequestClusterState => sender() ! ClusterStateResponse(cluster.state)
  }
}
