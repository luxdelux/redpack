package com.luxdelux.redpack.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.luxdelux.redpack.model.RPCRequest;
import com.luxdelux.redpack.model.RPCResponse;
import com.luxdelux.redpack.services.Service;

public class RPCServer {

  public static final String REQ_QUEUE_PREFIX = "redpack_request_queue:";
  public static final String RES_QUEUE_PREFIX = "redpack_response_queue:";
  public static final String RES_QUEUE_ID_KEY = "redpack_response_queue_index";

  private String redisHost;
  private String redisQueueName;
  private ExecutorService executorService;
  private RPCServerHandler handler;

  public RPCServer(String name) {
    this("localhost", name);
  }

  public RPCServer(String redisHost, String name) {
    this.redisHost = redisHost;
    this.redisQueueName = REQ_QUEUE_PREFIX + name;
    this.executorService = Executors.newSingleThreadExecutor();
    this.handler = new RPCServerHandler(redisHost, redisQueueName);
  }

  public void start() {
    writePIDToDisk();

    Thread workerThread = new Thread() {
      public void run() {
        executorService.submit(handler);
      }
    };
    workerThread.start();
  }

  public void stop() {
    executorService.shutdown();
    try {
      executorService.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
    } finally {
      deletePIDFromDisk();
    }
  }
  
  public void registerService(String methodName, Service service) {
    handler.registerService(methodName, service);
  }

  public String getPIDFileName() {
    return "/tmp/java_redpack.pid";
  }

  private void writePIDToDisk() {
    try {
      FileWriter fstream = new FileWriter(getPIDFileName());
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(lookupPID());
      // TODO Log PID
      out.close();
    } catch (IOException e) {
      // TODO
    }
  }

  private void deletePIDFromDisk() {
    File file = new File(getPIDFileName());
    if (file.exists()) {
      file.delete();
    }
  }

  /**
   * Unix-specific method of retrieving this server's process ID;
   * http://blog.igorminar.com/2007/03/how-java-application-can-discover-its.html
   */
  private String lookupPID() throws IOException {
    String[] cmd = {"bash", "-c", "echo $PPID"};
    Process p = Runtime.getRuntime().exec(cmd);

    int cnt = 0;
    int value = p.getInputStream().read();
    ByteBuffer buffer = ByteBuffer.allocate(10);
    while (value != -1) {
      cnt++;
      buffer.put((byte) value);
      value = p.getInputStream().read();
    }

    // Return copy of array to avoid writing junk bytes to PID file
    return new String(Arrays.copyOf(buffer.array(), cnt));
  }

  public static void main(String[] args) {
    final RPCServer server = new RPCServer(args[1]);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() { server.stop(); }
    });
    server.start();
  }
}
