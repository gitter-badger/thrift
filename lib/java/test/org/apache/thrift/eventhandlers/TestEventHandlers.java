package org.apache.thrift.eventhandlers;

import junit.framework.TestCase;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorEventHandler;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import thrift.test.Service;

import java.util.ArrayList;

public class TestEventHandlers extends TestCase {

  public static final int SERVER_PORT = 9090;
  public static final int TIMEOUT_MS = 250;

  public TServer newServer(TServerEventHandler serverEventHandler,
                           TProcessorEventHandler... processorEventHandlers)
    throws Exception {
    TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(SERVER_PORT, TIMEOUT_MS);

    TProcessor processor = new Service.Processor<Service.Iface>(new TestServiceHandler());
    for (TProcessorEventHandler handler : processorEventHandlers) {
      processor.addEventHandler(handler);
    }

    THsHaServer.Args args =
      new THsHaServer.Args(serverSocket).processor(processor)
                                        .protocolFactory(new TBinaryProtocol.Factory())
                                        .workerThreads(1);

    TServer server = new THsHaServer(args);
    if (serverEventHandler != null) {
      server.setServerEventHandler(serverEventHandler);
    }

    return server;
  }

  public static TestServerThread newServerThread(final TServer server) {
    return new TestServerThread(server);
  }

  public static Service.Client newClient() throws Exception {
    TSocket socket = new TSocket("localhost", 9090);
    socket.open();
    socket.setTimeout(TIMEOUT_MS);

    TTransport transport = new TFramedTransport(socket);
    TProtocol protocol = new TBinaryProtocol(transport);

    return new Service.Client(protocol);
  }

  public static TestServerEventHandler newServerEventHandler() {
    return new TestServerEventHandler();
  }

  public static TProcessorEventHandler newProcessorEventHandler() {
    return new TestProcessorEventHandler();
  }

  public Iterable<TProcessorEventHandler> singleProcessorEventHandler(TProcessorEventHandler handler) {
    ArrayList<TProcessorEventHandler> handlers = new ArrayList<TProcessorEventHandler>();
    handlers.add(handler);
    return handlers;
  }

  public void cleanupConnections(Service.Client client, TServer server, Thread serverThread) throws InterruptedException {
    client.getOutputProtocol().getTransport().close();
    server.stop();
    serverThread.join();
  }

  public void testServerHandlers() throws Exception {
    TestServerEventHandler serverEventHandler = newServerEventHandler();
    TestServerContext context = serverEventHandler.serverContext;

    TServer server = newServer(serverEventHandler, newProcessorEventHandler());
    TestServerThread serverThread = newServerThread(server).startAndWaitForServing();

    Service.Client client = newClient();

    long ret = client.mymethod(1);
    assertEquals(2, ret);

    cleanupConnections(client, server, serverThread);

    // Check per-server and per-connection counters
    assertEquals(1, context.preServeCounter.get());
    assertEquals(1, context.newConnectionContextCounter.get());
    assertEquals(1, context.deleteConnectionContextCounter.get());

    // Check per-request counters
    assertEquals(1, context.newProcessorContextCounter.get());
    assertTrue(context.allCallsSucceeded());

    // Check arguments seen by handler
    Service.mymethod_args args = context.lastArgs.get();
    assertEquals((long) 1, args.getFieldValue(Service.mymethod_args._Fields.BLAH));

    // Check returns seen by handler
    Service.mymethod_result result = context.lastResult.get();
    assertEquals((long) 2, result.getFieldValue(Service.mymethod_result._Fields.SUCCESS));
  }

  public void testTwoRequests() throws Exception {
    TestServerEventHandler serverEventHandler = newServerEventHandler();
    TestServerContext context = serverEventHandler.serverContext;

    TServer server = newServer(serverEventHandler, newProcessorEventHandler());
    TestServerThread serverThread = newServerThread(server).startAndWaitForServing();

    Service.Client client = newClient();

    client.mymethod(1);
    client.mymethod(2);

    cleanupConnections(client, server, serverThread);

    // Check per-server and per-connection counters
    assertEquals(1, context.preServeCounter.get());
    assertEquals(1, context.newConnectionContextCounter.get());
    assertEquals(1, context.deleteConnectionContextCounter.get());

    // Check per-request counters
    assertEquals(2, context.newProcessorContextCounter.get());
    assertTrue(context.allCallsSucceeded());
  }

  public void testTwoClients() throws Exception {
    TestServerEventHandler serverEventHandler = newServerEventHandler();
    TestServerContext context = serverEventHandler.serverContext;

    TServer server = newServer(serverEventHandler, newProcessorEventHandler());
    TestServerThread serverThread = newServerThread(server).startAndWaitForServing();

    Service.Client client = newClient();
    client.mymethod(1);
    client.getOutputProtocol().getTransport().close();

    client = newClient();
    client.mymethod(2);

    cleanupConnections(client, server, serverThread);

    // Check per-server and per-connection counters
    assertEquals(1, context.preServeCounter.get());
    assertEquals(2, context.newConnectionContextCounter.get());
    assertEquals(2, context.deleteConnectionContextCounter.get());

    // Check per-request counters
    assertEquals(2, context.newProcessorContextCounter.get());
    assertTrue(context.allCallsSucceeded());
  }

  public void testTwoHandlers() throws Exception {
    TestServerEventHandler serverEventHandler = newServerEventHandler();
    TestServerContext context = serverEventHandler.serverContext;

    TServer server = newServer(serverEventHandler,
                               newProcessorEventHandler(),
                               newProcessorEventHandler());
    TestServerThread serverThread = newServerThread(server).startAndWaitForServing();

    Service.Client client = newClient();
    client.mymethod(1);

    cleanupConnections(client, server, serverThread);

    // Check per-server and per-connection counters
    assertEquals(1, context.preServeCounter.get());
    assertEquals(1, context.newConnectionContextCounter.get());
    assertEquals(1, context.deleteConnectionContextCounter.get());

    // Check per-request counters
    assertEquals(1, context.newProcessorContextCounter.get());
    assertTrue(context.allCallsSucceeded(2));
  }

  public void testErrorHandler() throws Exception {
    TApplicationException savedException = new TApplicationException(TApplicationException.UNKNOWN);
    TestServerEventHandler serverEventHandler = newServerEventHandler();
    TestServerContext context = serverEventHandler.serverContext;

    TServer server = newServer(serverEventHandler, newProcessorEventHandler());
    TestServerThread serverThread = newServerThread(server).startAndWaitForServing();

    Service.Client client = newClient();
    try {
      // Server handler is coded to throw an unexpected exception (which will be converted to
      // TApplicationException and written to the client) on MAX_VALUE
      client.mymethod(Long.MAX_VALUE);
    } catch (TApplicationException exception) {
      // This exception is expected, what we really care about though is the errorCounter
      // check, and the comparison of this exception to the one the handler sees, both below.
      savedException = exception;
    }

    cleanupConnections(client, server, serverThread);

    // Check per-server and per-connection counters
    assertEquals(1, context.preServeCounter.get());
    assertEquals(1, context.newConnectionContextCounter.get());
    assertEquals(1, context.deleteConnectionContextCounter.get());

    // Check per-request counters
    assertEquals(1, context.newProcessorContextCounter.get());
    assertTrue(!context.allCallsSucceeded());
    assertEquals(1, context.errorCounter.get());

    // Server handler should have seen an exception, and it should have the same error code
    // as the one the client saw (though it won't be the same instance, because the server
    // exception gets serialized and then de-serialized by the client).
    TApplicationException lastException = ((TApplicationException) context.lastException.get());
    assertNotNull(lastException);
    assertEquals(savedException.getType(), lastException.getType());
  }

}
