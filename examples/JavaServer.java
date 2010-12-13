import com.luxdelux.redpack.server.*;
import com.luxdelux.redpack.services.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.msgpack.MessagePackObject;

public class JavaServer {
  static class MyEchoService implements Service {
    @Override
    public Object execute(Object... params) throws ServiceException {
  	  System.out.println("in echo");
  	  return "<from java: \"" + ((MessagePackObject) params[0]).asString() + "\">";
    }
  }
  
  public static void main(String[] args) throws Exception {
    MsgpackRPCServer server = new MsgpackRPCServer("queue_name");
    server.registerService("echo", new MyEchoService());
    server.start();
  }
}