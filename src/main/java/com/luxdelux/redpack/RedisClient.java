package com.luxdelux.redpack;

import java.io.IOException;
import java.util.List;

import redis.clients.jedis.Jedis;

public class RedisClient {

  public static final int DEFAULT_PORT = 6379;
  public static final String DEFAULT_HOST = "127.0.0.1";

  public static final int DEFAULT_BLPOP_TIMEOUT_S = 1;
  public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 2000;

  private Jedis client;

  public RedisClient() {
    this(DEFAULT_HOST);
  }

  public RedisClient(String host) {
    this(host, DEFAULT_PORT);
  }

  public RedisClient(String host, int port) {
    client = new Jedis(host, port, DEFAULT_CONNECTION_TIMEOUT_MS);
  }

  public void connect() {
    try {
      client.connect();
    } catch (Exception e) {
      throw new RedisException("Failed to connect to redis-server");
    }
  }

  public void disconnect() {
    try {
      client.disconnect();
    } catch (IOException e) {
      throw new RedisException("Failed to disconnect from redis-server");
    }
  }
  
  public String responseKeyName(String name) {
	  return client.incr(name).toString();
  }

  public long rpush(String queue, byte[] bytes) {
    return client.rpush(queue.getBytes(), bytes);
  }

  public byte[] blpop(String queue) {
    List<byte[]> reply = client.blpop(DEFAULT_BLPOP_TIMEOUT_S, queue.getBytes());
    if (reply != null && reply.size() == 2) {
    	return reply.get(1);
    }

    return null;
  }
}
