import com.luxdelux.redpack.server.*;
import com.luxdelux.redpack.services.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JavaServer {
  public static void main(String[] args) throws Exception {
    MsgpackRPCServer server = new MsgpackRPCServer("test");
    server.registerService("echo", new EchoService());
    server.start();
  }
}