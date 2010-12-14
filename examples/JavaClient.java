import com.luxdelux.redpack.server.*;
import com.luxdelux.redpack.client.*;
import com.luxdelux.redpack.services.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.msgpack.MessagePackObject;

public class JavaClient {
  public static void main(String[] args) throws Exception {
    RPCClient client = new RPCClient("queue_name");
    Object result = client.invoke("echo", "something");
    System.out.println("result : "+String.valueOf(result));
  }
}