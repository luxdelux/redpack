package com.luxdelux.redpack.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessageTypeException;

import com.luxdelux.redpack.MsgpackClient;
import com.luxdelux.redpack.PackException;
import com.luxdelux.redpack.RedisClient;
import com.luxdelux.redpack.UnpackException;
import com.luxdelux.redpack.model.RPCRequest;
import com.luxdelux.redpack.model.RPCResponse;
import com.luxdelux.redpack.services.Service;
import com.luxdelux.redpack.services.ServiceException;

public class MsgpackRPCServerHandler implements Runnable {

  private boolean killed;
  private String redisQueue;
  private RedisClient redisClient;
  private MsgpackClient msgpackClient;
  private Map<String, Service> services;

  public MsgpackRPCServerHandler(String redisHost, String redisQueue) {
    this(redisHost, redisQueue, null);
  }

  public MsgpackRPCServerHandler(String redisHost, String redisQueue, Map<String, Service> services) {
    this.killed = false;
    this.redisQueue = redisQueue;
    this.redisClient = new RedisClient(redisHost);
    this.msgpackClient = new MsgpackClient();

    if (services == null) {
      this.services = new HashMap<String, Service>(2);
      registerService("hello", new HelloService());
      registerService("add", new AddService());
    } else {
      this.services = services;
    }
    redisClient.connect();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() { killed = true; }
    });
  }

  @Override
  public void run() {
    do {
      byte[] top = redisClient.blpop(redisQueue); 
      if (top == null) {
        //return;
        continue;
      }
      
      RPCRequest request = null;
			try {
				request = msgpackClient.unpackRequest(top);
			} catch (UnpackException e) {
				// TODO Handle better
				e.printStackTrace();
				// return;
				continue;
			} catch (MessageTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
      Service service = services.get(request.getMethodName());
      if (service == null) {
        // TODO Handle better
        //return;
        continue;
      }

      String error = null;
      Object result = null;
      try {
        result = service.execute(request.getParameters());
      } catch (ServiceException e) {
        error = e.getMessage();
      }

      if (request.getResponseQueue() != null) {
        try {
          RPCResponse response = new RPCResponse(request, result, error);
          byte[] outData = msgpackClient.packResponse(response);
//          for(int i=0; i<outData.length; i++) {
//				System.out.println("out : "+String.format("%#06x ", outData[i]));
//			}

          redisClient.rpush(request.getResponseQueue(), outData);
        } catch (PackException e) {
          // TODO Handle better
          //return;
			e.printStackTrace();
          continue;
        } catch (IOException e) {
			e.printStackTrace();
			continue;
		}
        // TODO killWhenReady
      } else {
        // TODO killWhenReady
      }
    } while (!killed);
    
  }

  public void registerServices(Map<String, Service> services) {
    for (Map.Entry<String, Service> entry : services.entrySet()) {
      registerService(entry.getKey(), entry.getValue());
    }
  }

  public void registerService(String name, Service service) {
    services.put(name, service);
  }

  @Override
  protected void finalize() throws Throwable {
    redisClient.disconnect();
    super.finalize();
  }
}

final class HelloService implements Service {

  @Override
  public Object execute(Object... params) throws ServiceException {
    if (params.length != 1) {
      throw new ServiceException("HelloService expects 1 parameter");
    }

    String result = "yo " + (String) params[0];
    // TODO Log result
    return result;
  }
}

final class AddService implements Service {

  @Override
  public Object execute(Object... params) throws ServiceException {
    if (params.length != 2) {
      throw new ServiceException("AddService expects 2 parameters");
    }

    long a = (Long) params[0];
    long b = (Long) params[1];
    long sum = a + b;
    // TODO Log sum
    return sum;
  }
}
