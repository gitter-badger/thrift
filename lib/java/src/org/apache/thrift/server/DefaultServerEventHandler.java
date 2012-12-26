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

import org.apache.thrift.TProcessorContext;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportFactory;

/**
 * Default implementation of {@link TServerEventHandler} that just creates basic
 * {@link TProcessorContext} instances for {@link org.apache.thrift.TProcessorEventHandler}
 */
public class DefaultServerEventHandler implements TServerEventHandler {
  @Override
  public void preServe() {
  }

  @Override
  public ServerContext newConnectionContext(TTransport inputTransport,
                                            TTransport outputTransport,
                                            TTransportFactory inputTransportFactory,
                                            TTransportFactory outputTransportFactory,
                                            TProtocolFactory inputProtocolFactory,
                                            TProtocolFactory outputProtocolFactory) {
    return null;
  }

  /**
   * Override this method to return a subclass of {@link org.apache.thrift.TProcessorContext} in order to pass
   * information from a {@link TServerEventHandler} to a {@link org.apache.thrift.TProcessorEventHandler}
   *
   * @param connectionContext
   * @param inputProtocol
   * @param outputProtocol
   * @return A serverContext that will be passed to the {@link org.apache.thrift.TProcessorEventHandler}
   *
   */
  @Override
  public TProcessorContext newProcessorContext(ServerContext connectionContext,
                                               TProtocol inputProtocol,
                                               TProtocol outputProtocol) {
    return new TProcessorContext(inputProtocol, outputProtocol);
  }

  @Override
  public void deleteConnectionContext(ServerContext connectionContext) {
  }
}
