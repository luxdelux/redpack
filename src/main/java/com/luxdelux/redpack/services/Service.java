package com.luxdelux.redpack.services;


public interface Service {

  public Object execute(Object... params) throws ServiceException;
}
