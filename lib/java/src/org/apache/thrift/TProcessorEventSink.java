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

import java.util.ArrayList;
import java.util.List;

/**
 * A per-request event sink that handles dispatching events from the processor to all of the
 * {@link TProcessorEventHandler} instances attached to it for each event of the request.
 */
public class TProcessorEventSink {
  private final List<HandlerEntry> entries;

  private static class HandlerEntry {
    private final TProcessorEventHandler handler;
    private final Object context;

    public HandlerEntry(TProcessorEventHandler handler, Object context) {
      this.handler = handler;
      this.context = context;
    }
  }

  public TProcessorEventSink(TProcessorContext processorContext,
                             String methodName,
                             List<TProcessorEventHandler> handlers) {
    this.entries = new ArrayList<HandlerEntry>(handlers.size());
    for (TProcessorEventHandler handler : handlers) {
      this.entries.add(new HandlerEntry(handler,
                                        handler.newHandlerContext(processorContext, methodName)));
    }
  }

  public void preRead() {
    for (HandlerEntry entry : entries) {
      entry.handler.preRead(entry.context);
    }
  }

  public void postRead(TBase args) {
    for (HandlerEntry entry : entries) {
      entry.handler.postRead(entry.context, args);
    }
  }

  public void preWrite(TBase result) {
    for (HandlerEntry entry : entries) {
      entry.handler.preWrite(entry.context, result);
    }
  }

  public void postWrite(TBase result) {
    for (HandlerEntry entry : entries) {
      entry.handler.postWrite(entry.context, result);
    }
  }

  public void processorError(Throwable throwable) {
    for (HandlerEntry entry : entries) {
      entry.handler.processorError(entry.context, throwable);
    }
  }

  public void deleteRequestContexts() {
    for (HandlerEntry entry : entries) {
      entry.handler.deleteRequestContext(entry.context);
    }
  }
}
