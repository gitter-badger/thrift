package org.apache.thrift.eventhandlers;

import org.apache.thrift.TProcessorContext;
import org.apache.thrift.protocol.TProtocol;
import org.junit.Ignore;

@Ignore
public class TestProcessorContext extends TProcessorContext {
  public final TestServerContext serverContext;

  public TestProcessorContext(TestServerContext context, TProtocol inputProtocol, TProtocol outputProtocol) {
    super(inputProtocol, outputProtocol);
    this.serverContext = context;
  }
}
