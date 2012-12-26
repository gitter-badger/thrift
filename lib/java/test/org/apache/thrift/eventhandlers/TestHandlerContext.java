package org.apache.thrift.eventhandlers;

import org.junit.Ignore;

@Ignore
public class TestHandlerContext {
  public final TestServerContext serverContext;

  public TestHandlerContext(TestServerContext context) {
    this.serverContext = context;
  }
}
