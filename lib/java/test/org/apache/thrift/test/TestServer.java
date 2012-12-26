/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.thrift.test;

import org.apache.thrift.TBase;
import org.apache.thrift.TProcessorContext;
import org.apache.thrift.TProcessorEventHandler;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.ServerTestBase.TestHandler;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportFactory;
import thrift.test.ThriftTest;

public class TestServer {

  static class TestServerContext implements ServerContext {
    private final int connectionId;
    public TestServerContext(int connectionId) {
      this.connectionId = connectionId;
    }

    public int getConnectionId() {
      return connectionId;
    }
  }

  static class TestProcessorContext extends TProcessorContext {
    private final int connectionId;

    public TestProcessorContext(TProtocol inputProtocol,
                                TProtocol outputProtocol,
                                int connectionId) {
      super(inputProtocol, outputProtocol);
      this.connectionId = connectionId;
    }

    public int getConnectionId() {
      return connectionId;
    }
  }

  private static class TestHandlerContext {
    private final int connectionId;

    private final String methodName;
    private TestHandlerContext(int connectionId, String methodName) {
      this.connectionId = connectionId;
      this.methodName = methodName;
    }

    public int getConnectionId() {
      return connectionId;
    }

    public String getMethodName() {
      return methodName;
    }
  }

  static class TestServerEventHandler implements TServerEventHandler {

    private int nextConnectionId = 1;

    public void preServe() {
      System.out.println("TServerEventHandler.preServe - called only once before server starts accepting connections");
    }

    @Override
    public ServerContext newConnectionContext(TTransport inputTransport,
                                              TTransport outputTransport,
                                              TTransportFactory inputTransportFactory,
                                              TTransportFactory outputTransportFactory,
                                              TProtocolFactory inputProtocolFactory,
                                              TProtocolFactory outputProtocolFactory) {
      //we can create some connection level data which is stored while connection is alive & served
      TestServerContext ctx = new TestServerContext(nextConnectionId++);
      System.out.println(
        "TServerEventHandler.newHandlerContext - connection #" + ctx.getConnectionId() +
        " established");
      return ctx;
    }

    @Override
    public void deleteConnectionContext(ServerContext connectionContext) {
      TestServerContext ctx = (TestServerContext) connectionContext;
      System.out.println(
        "TServerEventHandler.deleteRequestContext - connection #" + ctx.getConnectionId() +
        " terminated");
    }
    @Override
    public TProcessorContext newProcessorContext(ServerContext connectionContext, TProtocol inputProtocol, TProtocol outputProtocol) {
      TestServerContext ctx = (TestServerContext) connectionContext;
      System.out.println(
        "TServerEventHandler.processContext - connection #" + ctx.getConnectionId() +
        " is ready to process next request");
      return new TestProcessorContext(inputProtocol, outputProtocol, ctx.getConnectionId());
    }

  }

  static class TestProcessorEventHandler implements TProcessorEventHandler {

    @Override
    public Object newHandlerContext(TProcessorContext processorContext, String methodName) {
      TestProcessorContext context = (TestProcessorContext) processorContext;
      return new TestHandlerContext(context.getConnectionId(), methodName);
    }

    @Override
    public void preRead(Object context) {
      TestHandlerContext processorContext = (TestHandlerContext) context;
      System.out.println(
        "TServerEventHandler.processContext - connection #" + processorContext.getConnectionId() +
        " reading args for method " + processorContext.getMethodName());
    }

    @Override
    public void postRead(Object context, TBase args) {
      TestHandlerContext processorContext = (TestHandlerContext) context;
      System.out.println(
        "TServerEventHandler.processContext - connection #" + processorContext.getConnectionId() +
        " finished reading args: " + args.toString());
    }

    @Override
    public void preWrite(Object context, TBase result) {
      TestHandlerContext processorContext = (TestHandlerContext) context;
      System.out.println(
        "TServerEventHandler.processContext - connection #" + processorContext.getConnectionId() +
        " writing result for method " + processorContext.getMethodName());
    }

    @Override
    public void postWrite(Object context, TBase result) {
      TestHandlerContext processorContext = (TestHandlerContext) context;
      System.out.println(
        "TServerEventHandler.processContext - connection #" + processorContext.getConnectionId() +
        " finished writing result: " + result.toString());
    }

    @Override
    public void processorError(Object context, Throwable throwable) {
    }

    @Override
    public void deleteRequestContext(Object context) {
    }
  }

  public static void main(String[] args) {
    try {
      int port = 9090;
      if (args.length > 1) {
        port = Integer.valueOf(args[0]);
      }
      //@TODO add other protocol and transport types

      // Processor
      TestHandler testHandler =
        new TestHandler();
      ThriftTest.Processor testProcessor =
        new ThriftTest.Processor<ThriftTest.Iface>(testHandler);

      testProcessor.addEventHandler(new TestProcessorEventHandler());

      // Transport
      TServerSocket tServerSocket =
        new TServerSocket(port);

      // Protocol factory
      TProtocolFactory tProtocolFactory =
        new TBinaryProtocol.Factory();

      TServer serverEngine;

      // Simple Server
      //serverEngine = new TSimpleServer(new Args(tServerSocket).processor(testProcessor));

      // ThreadPool Server
      serverEngine = new TThreadPoolServer(new TThreadPoolServer.Args(tServerSocket).processor(testProcessor).protocolFactory(tProtocolFactory));

      //Set server event handler
      serverEngine.setServerEventHandler(new TestServerEventHandler());

      // Run it
      System.out.println("Starting the server on port " + port + "...");
      serverEngine.serve();

    } catch (Exception x) {
      x.printStackTrace();
    }
    System.out.println("done.");
  }
}
