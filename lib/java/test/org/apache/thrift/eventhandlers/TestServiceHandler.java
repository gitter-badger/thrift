package org.apache.thrift.eventhandlers;

import org.apache.thrift.TException;
import org.junit.Ignore;
import thrift.test.Service;

@Ignore
public class TestServiceHandler implements Service.Iface {
  @Override
  public long mymethod(long blah) throws TException {
    if (blah == Long.MAX_VALUE) {
      throw new RuntimeException("Simulating unexpected runtime error!");
    }

    return blah + 1;
  }
}
