package org.apache.thrift.eventhandlers;

import org.apache.thrift.TBase;
import org.apache.thrift.TProcessorContext;
import org.apache.thrift.TProcessorEventHandler;
import org.junit.Ignore;
import thrift.test.Service;

@Ignore
class TestProcessorEventHandler implements TProcessorEventHandler {
  @Override
  public Object newHandlerContext(TProcessorContext processorContext, String methodName) {
    TestProcessorContext context = (TestProcessorContext) processorContext;
    context.serverContext.newHandlerContextCounter.incrementAndGet();
    return new TestHandlerContext(context.serverContext);
  }

  @Override
  public void preRead(Object handlerContext) {
    TestHandlerContext context = (TestHandlerContext) handlerContext;
    context.serverContext.preReadCounter.incrementAndGet();
  }

  @Override
  public void postRead(Object handlerContext, TBase args) {
    TestHandlerContext context = (TestHandlerContext) handlerContext;
    context.serverContext.lastArgs.set((Service.mymethod_args)args);
    context.serverContext.postReadCounter.incrementAndGet();
  }

  @Override
  public void preWrite(Object handlerContext, TBase result) {
    TestHandlerContext context = (TestHandlerContext) handlerContext;
    context.serverContext.lastResult.set((Service.mymethod_result)result);
    context.serverContext.preWriteCounter.incrementAndGet();
  }

  @Override
  public void postWrite(Object handlerContext, TBase result) {
    TestHandlerContext context = (TestHandlerContext) handlerContext;
    context.serverContext.postWriteCounter.incrementAndGet();
  }

  @Override
  public void processorError(Object handlerContext, Throwable throwable) {
    TestHandlerContext context = (TestHandlerContext) handlerContext;
    context.serverContext.lastException.set(throwable);
    context.serverContext.errorCounter.incrementAndGet();
  }

  @Override
  public void deleteRequestContext(Object handlerContext) {
    TestHandlerContext context = (TestHandlerContext) handlerContext;
    context.serverContext.deleteHandlerContextCounter.incrementAndGet();
  }
}
