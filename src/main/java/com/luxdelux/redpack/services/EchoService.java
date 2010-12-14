package com.luxdelux.redpack.services;

public class EchoService implements Service {

  public Object execute(Object... params) throws ServiceException {
	  System.out.println("in echo");
    if (params.length > 0) {
      return params[0];
    } else {
      return "no params";
    }
  }
}
