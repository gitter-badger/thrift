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

/**
 * Interface that can handle events from a server processor core. To use this interface, you should
 * subclass it and implement the methods you care about. Your handler may be called from the server
 * on multiple threads, and so it must be thread-safe. You can share state between all the events
 * fired for a given request by storing it on the serverContext object you create when
 * {@link TProcessorEventHandler#newHandlerContext} is called.
 */
public interface TProcessorEventHandler {

  /**
   * Handler callback invoked when the processor begins to process a request, just after the
   * {@link org.apache.thrift.protocol.TMessage} has been read (so the name of the method
   * is known.)
   * <p/>
   * When running in a server with a custom {@link org.apache.thrift.server.TServerEventHandler},
   * the {@code processorContext} argument may be a custom subclass of TProcessorContext with
   * extra information furnished by the {@link org.apache.thrift.server.TServerEventHandler}.
   *
   * @param processorContext
   * @param methodName
   * @return A handler-specific serverContext object which will be passed to the other handler methods
   *         during the course of processing the event.
   */
  Object newHandlerContext(TProcessorContext processorContext, String methodName);

  /**
   * Handler callback invoked just before the processor reads the arguments portion of the request.
   *
   * @param context
   */
  void preRead(Object context);

  /**
   * Handler callback invoked just after the processor has read the arguments portion of the
   * request, just before the request is run on the server.
   *
   * @param context
   */
  void postRead(Object context, TBase args);

  /**
   * Handler callback invoked just before the response is written, after the processor has finished
   * running the request on the server.
   *
   * @param context
   * @param result
   */
  void preWrite(Object context, TBase result);

  /**
   * Handler callback invoked just after the processor has written the response back to the client.
   *
   * @param context
   * @param result
   */
  void postWrite(Object context, TBase result);

  /**
   * Handler callback invoked when something has gone wrong while handling the request (e.g.
   * a {@link org.apache.thrift.protocol.TProtocolException} or {@link TApplicationException}
   * has been thrown).
   *
   * @param context
   * @param throwable
   */
  void processorError(Object context, Throwable throwable);

  /**
   * Handler callback invoked when the processor is done with the request any state initialized
   * in {@link TProcessorEventHandler#newHandlerContext} that will
   * not be automatically handled by the garbage collector should be cleaned here.
   *
   * @param context
   */
  void deleteRequestContext(Object context);
}
