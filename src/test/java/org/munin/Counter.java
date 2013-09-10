package org.munin;

public class Counter implements CounterMBean {

  private int counter = 0;

  public int increment() {
    return counter++;
  }

}
