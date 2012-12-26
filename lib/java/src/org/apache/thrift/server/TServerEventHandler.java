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
 * Interface that can handle events from the server core. To
 * use this you should subclass it and implement the methods that you care
 * about. Your subclass can also store local data that you may care about,
 * such as additional "arguments" to these methods (stored in the object
 * instance's state).
 */
public interface TServerEventHandler {
  /**
   * Called before the server begins.
   */
  void preServe();

  /**
   * Called when a new client has connected and is about to being processing. Transport
   * and protocol factories are provided in case the handler needs to do some custom communication
   * with the client.
   *
   * @param inputTransport         the raw connection used to receive input from the client
   * @param outputTransport        the raw connection used to send output to the client
   * @param inputTransportFactory  used by handlers that will need to read messages
   * @param outputTransportFactory used by handlers that will need to read messages
   * @param inputProtocolFactory   used by handlers that will need to read messages
   * @param outputProtocolFactory  used by handlers that will need to write messages
   */
  ServerContext newConnectionContext(TTransport inputTransport,
                                     TTransport outputTransport,
                                     TTransportFactory inputTransportFactory,
                                     TTransportFactory outputTransportFactory,
                                     TProtocolFactory inputProtocolFactory,
                                     TProtocolFactory outputProtocolFactory);

  /**
   * Called just before the processor is invoked, to create a serverContext for the
   * {@link org.apache.thrift.TProcessorEventHandler}.
   * <p/>
   * Override this method to return a subclass of {@link org.apache.thrift.TProcessorContext} in order to pass
   * information from a {@link TServerEventHandler} to a {@link org.apache.thrift.TProcessorEventHandler}
   *
   * @param connectionContext
   * @param inputProtocol
   * @param outputProtocol
   * @return A serverContext used by {@link org.apache.thrift.TProcessorEventHandler}
   */
  TProcessorContext newProcessorContext(ServerContext connectionContext,
                                        TProtocol inputProtocol,
                                        TProtocol outputProtocol);

  /**
   * Called when a client has finished request-handling to delete server
   * serverContext.
   */
  void deleteConnectionContext(ServerContext connectionContext);
}