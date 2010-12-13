package com.luxdelux.redpack;

import java.io.IOException;

@SuppressWarnings("serial")
public class RedisException extends RuntimeException {

  public RedisException(IOException e) {
    super(e);
  }

  public RedisException(String message) {
    super(message);
  }

  public RedisException(String message, Throwable cause) {
    super(message, cause);
  }
}
