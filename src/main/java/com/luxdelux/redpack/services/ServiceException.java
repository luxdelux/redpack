package com.luxdelux.redpack.services;

@SuppressWarnings("serial")
public class ServiceException extends Exception {

  public ServiceException() {
    super();
  }

  public ServiceException(String message) {
    super(message);
  }
}
