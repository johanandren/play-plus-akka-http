/**
 * Copyright (C) 2009-2018 Lightbend Inc. <http://www.lightbend.com>
 */
package trixytrix

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.http.javadsl.server.AccessToRequestContext
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.ByteString
import play.api.http.HttpEntity.{Streamed, Strict}
import play.api.libs.streams.Accumulator
import play.api.mvc.{Action, EssentialAction, ResponseHeader, Result}
import play.api.routing.Router.Routes
import play.api.routing.{Router, SimpleRouter}
import play.core.server.akkahttp.AkkaHeadersWrapper

import scala.concurrent.Promise

class AkkaHttpRouter @Inject()(implicit system: ActorSystem, mat: Materializer, prefix: String = "") extends SimpleRouter {

  val routingSettings = RoutingSettings(system)
  import system.dispatcher

  val route =
    pathPrefix(prefix.drop(1)) {
      path("banana") {
        extractDataBytes { bytes =>
          onComplete(bytes.runWith(Sink.foreach(b => println(b.utf8String)))) { _ =>
            complete("woho!")
          }
        }

      }
    }

  override def routes: Routes = {

    case request if request.path.startsWith(prefix) =>
      EssentialAction { request =>

        // the accumulator accepts a sink that will be fed the data from the request
        val accumulator: Accumulator[ByteString, RouteResult] =
          Accumulator(Sink.asPublisher[ByteString](false).mapMaterializedValue { publisher =>

            val akkaRequest = request.headers.asInstanceOf[AkkaHeadersWrapper].request
            val entitySource = Source.fromPublisher(publisher)
            val requestWithBytesReintroduced = akkaRequest.withEntity(
              HttpEntity(akkaRequest.entity.contentType, akkaRequest.entity.contentLengthOption.get, entitySource)
            )

            route(AccessToRequestContext(requestWithBytesReintroduced, system.log, routingSettings)(system.dispatcher, mat))
          })

        accumulator.map {
          case RouteResult.Complete(httpResponse) =>
            Result(
              ResponseHeader(
                httpResponse.status.intValue(),
                httpResponse.headers.map(h => h.name() -> h.value()).toMap
              ),
              Streamed(httpResponse.entity.dataBytes, httpResponse.entity.contentLengthOption, Some(httpResponse.entity.contentType.toString()))
            )
          case RouteResult.Rejected(rejections) =>
            Result(ResponseHeader(503), Strict(ByteString(), None))
        }

      }
  }

  override def documentation: Seq[(String, String, String)] = Seq.empty

  override def withPrefix(prefix: String): Router = new AkkaHttpRouter()(system, mat, prefix)
}
