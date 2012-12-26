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

package org.apache.thrift;

import org.apache.thrift.protocol.TProtocol;

import java.net.InetAddress;

/**
 * Generic serverContext for the execution of a single request in a {@link TProcessor}.
 * <p/>
 * The input and output {@link TProtocol} instances in this generic context are those that
 * will actually be used by the {@link TProcessor} for handling the current request, and thus may
 * not wrap the actual connection {@link org.apache.thrift.transport.TTransport}. For example,
 * non-blocking servers make NIO connections to clients via
 * {@link org.apache.thrift.transport.TNonblockingTransport}, but the {@link TProcessor} is passed
 * {@link TProtocol} instances that wrap {@link org.apache.thrift.transport.TMemoryBuffer}
 * transports. If you need access to the {@link org.apache.thrift.transport.TTransport} for the
 * connection, you should implement a custom {@link org.apache.thrift.server.TServerEventHandler}.
 */
public class TProcessorContext {
  private final TProtocol inputProtocol;
  private final TProtocol outputProtocol;

  public TProcessorContext(TProtocol inputProtocol, TProtocol outputProtocol) {
    this.inputProtocol = inputProtocol;
    this.outputProtocol = outputProtocol;
  }

  public TProtocol getInputProtocol() {
    return inputProtocol;
  }

  public TProtocol getOutputProtocol() {
    return outputProtocol;
  }

  public InetAddress getPeerAddress() {
    return null;
  }
}
