import com.luxdelux.redpack.RPCClient;

public class JavaClient {
  public static void main(String[] args) throws Exception {
    RPCClient client = new RPCClient("queue_name");
    Object result = client.invoke("echo", "something from java", 722);
    System.out.println("result : "+String.valueOf(result));
  }
}
