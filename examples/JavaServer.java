import com.luxdelux.redpack.RPCServer;
import com.luxdelux.redpack.services.Service;
import com.luxdelux.redpack.services.ServiceException;

public class JavaServer {
  static class MyEchoService implements Service {
    @Override
    public Object execute(Object... params) throws ServiceException {
  	  System.out.println("in echo, params: " + params[0].toString());
  	  return "<from java: \"" + params[0].toString() + "\">";
    }
  }
  
  public static void main(String[] args) throws Exception {
    RPCServer server = new RPCServer("queue_name");
    server.registerService("echo", new MyEchoService());
    server.start();
  }
}