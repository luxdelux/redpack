package com.luxdelux.redpack.services;

public class EchoService implements Service {

  @Override
  public Object execute(Object... params) throws ServiceException {
	  System.out.println("in echo");
    if (params.length > 0) {
      return String.valueOf(params[0]);
    } else {
      return "no params";
    }
  }
}
