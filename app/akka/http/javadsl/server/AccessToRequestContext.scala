/**
 * Copyright (C) 2009-2018 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.http.javadsl.server

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.RequestContextImpl
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.Materializer

import scala.concurrent.ExecutionContextExecutor

object AccessToRequestContext {

  def apply(request: HttpRequest, log: LoggingAdapter, settings: RoutingSettings)(implicit ec: ExecutionContextExecutor, mat: Materializer) = new RequestContextImpl(request, log, settings)

}
