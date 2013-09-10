package org.munin;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class Agent {

  public static void main(String[] args) throws Exception {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    ObjectName name = new ObjectName("com.example:type=Hello");
    Counter mbean = new Counter();
    mbs.registerMBean(mbean, name);
    Thread.sleep(Long.MAX_VALUE);
  }

}
