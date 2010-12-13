package com.luxdelux.redpack;

@SuppressWarnings("serial")
public class PackException extends Exception {

  public PackException(String message) {
    super(message);
  }

  public PackException(Throwable e) {
    super(e);
  }
}
