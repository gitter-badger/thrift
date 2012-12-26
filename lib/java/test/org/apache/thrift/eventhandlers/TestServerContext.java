package org.apache.thrift.eventhandlers;

import org.apache.thrift.server.ServerContext;
import org.junit.Ignore;
import thrift.test.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Ignore
public class TestServerContext implements ServerContext {
  public final AtomicInteger preServeCounter = new AtomicInteger(0);
  public final AtomicInteger newConnectionContextCounter = new AtomicInteger(0);
  public final AtomicInteger newProcessorContextCounter = new AtomicInteger(0);
  public final AtomicInteger deleteConnectionContextCounter = new AtomicInteger(0);

  public final AtomicInteger newHandlerContextCounter = new AtomicInteger(0);
  public final AtomicInteger preReadCounter = new AtomicInteger(0);
  public final AtomicInteger postReadCounter = new AtomicInteger(0);
  public final AtomicInteger preWriteCounter = new AtomicInteger(0);
  public final AtomicInteger postWriteCounter = new AtomicInteger(0);
  public final AtomicInteger errorCounter = new AtomicInteger(0);
  public final AtomicInteger deleteHandlerContextCounter = new AtomicInteger(0);

  public final AtomicReference<Service.mymethod_args> lastArgs =
    new AtomicReference<Service.mymethod_args>();
  public final AtomicReference<Service.mymethod_result> lastResult =
    new AtomicReference<Service.mymethod_result>();
  public AtomicReference<Throwable> lastException =
    new AtomicReference<Throwable>();

  public boolean allCallsSucceeded() {
    return allCallsSucceeded(1);
  }

  public boolean allCallsSucceeded(int numberOfProcessors) {
    // Early-out if any calls explicitly failed
    if (errorCounter.get() > 0) {
      return false;
    }

    // Otherwise check that all counters for events that
    // happen during successful processing have equal counts.
    int callsStarted = newProcessorContextCounter.get();
    return (newHandlerContextCounter.get() == callsStarted * numberOfProcessors &&
            preReadCounter.get() == callsStarted * numberOfProcessors &&
            postReadCounter.get() == callsStarted * numberOfProcessors &&
            preWriteCounter.get() == callsStarted * numberOfProcessors &&
            postWriteCounter.get() == callsStarted * numberOfProcessors &&
            deleteHandlerContextCounter.get() == callsStarted * numberOfProcessors);
  }
}
