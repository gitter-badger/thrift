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

package org.apache.thrift.server;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generic interface for a Thrift server.
 *
 */
public abstract class TServer {

  public static class Args extends AbstractServerArgs<Args> {
    public Args(TServerTransport transport) {
      super(transport);
    }
  }

  public static abstract class AbstractServerArgs<T extends AbstractServerArgs<T>> {
    final TServerTransport serverTransport;
    TProcessorFactory processorFactory;
    TTransportFactory inputTransportFactory = new TTransportFactory();
    TTransportFactory outputTransportFactory = new TTransportFactory();
    TProtocolFactory inputProtocolFactory = new TBinaryProtocol.Factory();
    TProtocolFactory outputProtocolFactory = new TBinaryProtocol.Factory();
    public AbstractServerArgs(TServerTransport transport) {
      serverTransport = transport;
    }

    public T processorFactory(TProcessorFactory factory) {
      this.processorFactory = factory;
      return (T) this;
    }

    public T processor(TProcessor processor) {
      this.processorFactory = new TProcessorFactory(processor);
      return (T) this;
    }

    public T transportFactory(TTransportFactory factory) {
      this.inputTransportFactory = factory;
      this.outputTransportFactory = factory;
      return (T) this;
    }

    public T inputTransportFactory(TTransportFactory factory) {
      this.inputTransportFactory = factory;
      return (T) this;
    }

    public T outputTransportFactory(TTransportFactory factory) {
      this.outputTransportFactory = factory;
      return (T) this;
    }

    public T protocolFactory(TProtocolFactory factory) {
      this.inputProtocolFactory = factory;
      this.outputProtocolFactory = factory;
      return (T) this;
    }

    public T inputProtocolFactory(TProtocolFactory factory) {
      this.inputProtocolFactory = factory;
      return (T) this;
    }

    public T outputProtocolFactory(TProtocolFactory factory) {
      this.outputProtocolFactory = factory;
      return (T) this;
    }
  }

  /**
   * Core processor
   */
  protected TProcessorFactory processorFactory_;

  /**
   * Server transport
   */
  protected TServerTransport serverTransport_;

  /**
   * Input Transport Factory
   */
  protected TTransportFactory inputTransportFactory_;

  /**
   * Output Transport Factory
   */
  protected TTransportFactory outputTransportFactory_;

  /**
   * Input Protocol Factory
   */
  protected TProtocolFactory inputProtocolFactory_;

  /**
   * Output Protocol Factory
   */
  protected TProtocolFactory outputProtocolFactory_;

  private boolean isServing;

  /**
   * Synchronizer used to implement {@link org.apache.thrift.server.TServer#waitForServing()}
   */
  private final Object isServingSynchronizer = new Object();

  protected TServerEventHandler eventHandler_;

  protected TServer(AbstractServerArgs args) {
    processorFactory_ = args.processorFactory;
    serverTransport_ = args.serverTransport;
    inputTransportFactory_ = args.inputTransportFactory;
    outputTransportFactory_ = args.outputTransportFactory;
    inputProtocolFactory_ = args.inputProtocolFactory;
    outputProtocolFactory_ = args.outputProtocolFactory;
    eventHandler_ = new DefaultServerEventHandler();
  }

  /**
   * The run method fires up the server and gets things going.
   */
  public abstract void serve();

  /**
   * Stop the server. This is optional on a per-implementation basis. Not
   * all servers are required to be cleanly stoppable.
   */
  public void stop() {}

  /**
   * Wait until isServing() becomes true
   *
   * @throws InterruptedException if the thread is interrupted before isServing() becomes true
   */
  public final void waitForServing() throws InterruptedException {
    synchronized (isServingSynchronizer) {
      while (!isServing()) {
        isServingSynchronizer.wait();
      }
    }
  }

  public final boolean isServing() {
    synchronized (isServingSynchronizer) {
      return isServing;
    }
  }

  protected final void setServing(boolean serving) {
    synchronized (isServingSynchronizer) {
      isServing = serving;
      if (serving) {
        isServingSynchronizer.notifyAll();
      }
    }
  }

  public void setServerEventHandler(TServerEventHandler eventHandler) {
    eventHandler_ = eventHandler;
  }

  public TServerEventHandler getEventHandler() {
    return eventHandler_;
  }
}
