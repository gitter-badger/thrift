package org.apache.thrift.eventhandlers;

import org.apache.thrift.server.TServer;
import org.junit.Ignore;

@Ignore
class TestServerThread extends Thread {
  private final TServer server;

  public TestServerThread(TServer server) {
    this.server = server;
  }

  @Override
  public void run() {
    server.serve();
  }

  public TestServerThread startAndWaitForServing() throws InterruptedException {
    start();
    server.waitForServing();
    return this;
  }
}
