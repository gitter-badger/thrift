package org.apache.thrift.eventhandlers;

import org.apache.thrift.TProcessorContext;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportFactory;
import org.junit.Ignore;

@Ignore
class TestServerEventHandler implements TServerEventHandler {
  public final TestServerContext serverContext = new TestServerContext();

  @Override
  public void preServe() {
    serverContext.preServeCounter.incrementAndGet();
  }

  @Override
  public ServerContext newConnectionContext(TTransport inputTransport, TTransport outputTransport, TTransportFactory inputTransportFactory, TTransportFactory outputTransportFactory, TProtocolFactory inputProtocolFactory, TProtocolFactory outputProtocolFactory) {
    serverContext.newConnectionContextCounter.incrementAndGet();
    return serverContext;
  }

  @Override
  public TProcessorContext newProcessorContext(ServerContext connectionContext, TProtocol inputProtocol, TProtocol outputProtocol) {
    TestServerContext context = (TestServerContext) connectionContext;
    context.newProcessorContextCounter.incrementAndGet();
    return new TestProcessorContext(context, inputProtocol, outputProtocol);
  }

  @Override
  public void deleteConnectionContext(ServerContext connectionContext) {
    TestServerContext context = (TestServerContext) connectionContext;
    context.deleteConnectionContextCounter.incrementAndGet();
  }
}
