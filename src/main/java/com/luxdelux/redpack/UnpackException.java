package com.luxdelux.redpack;

@SuppressWarnings("serial")
public class UnpackException extends Exception {

  public UnpackException(String message) {
    super(message);
  }

  public UnpackException(Throwable e) {
    super(e);
  }
}
