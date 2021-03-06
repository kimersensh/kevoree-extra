package com.twitter.finagle.tracing

/**
 * The TracingFilter takes care of span lifecycle events. It is always
 * placed first in the server filter chain so that protocols with
 * trace support will override the span resets, and still be properly
 * reported here.
 */

import com.twitter.finagle.{Service, SimpleFilter}

class TracingFilter[Req, Rep](tracer: Tracer)
  extends SimpleFilter[Req, Rep]
{
  def apply(request: Req, service: Service[Req, Rep]) = {
    Trace.unwind {
      Trace.pushTracer(tracer)
      val nextId = Trace.nextId
      if (Trace.id.sampled || tracer.sampleTrace(nextId)) {
        Trace.pushId(nextId.copy(sampled = true))
      } else {
        Trace.pushId(nextId)
      }
      service(request)
    }
  }
}
