RedPack - Easily Scalable RPC
=============================

Super simple RPC using a slightly modified form of MsgPack-RPC & BSON, on top of Redis queues.  Currently supports NodeJS, Ruby, and Java.


NodeJS examples
---------------

### Server:

    s = new rpc.Server('my_queue_name', {
      echo: function(amount) {
        return "from node server: " + (amount + 10);
      }
    });
    s.start();

### Client:

    c = new rpc.Client('my_queue_name');
    c.invoke('echo', [12], function(err, result) {
      console.log("result is: "+result);
    });


Ruby examples:
--------------

### Server:

    class ExampleService
      def echo(amount)
        puts "called add_amount"
        return "from ruby: #{amount + 2}"
      end
    end

    server = RedPack::Server.new("my_queue_name")
    server.listen(ExampleService.new)

### Client:

    client = RedPack::Client.new("my_queue_name")

    puts "making the method call asynchronously"
    client.echo_async(23) do |error, result|
      puts "async result:"
      p result
    end


Java examples:
--------------

### Server:

    public class EchoService implements Service {
      public Object execute(Object... params) throws ServiceException {
    	  System.out.println("in echo");
        return "echo!";
      }
    }

    MsgpackRPCServer server = new MsgpackRPCServer("my_queue_name");
    server.registerService("echo", new EchoService());
    server.start();

### Client:

    RPCClient client = new RPCClient("queue_name");
    Object result = client.invoke("echo", "something from java");
    System.out.println("result : "+String.valueOf(result));
