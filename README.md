RedPack
=============

Super simple RPC using a slightly modified form of MsgPack-RPC, on top of Redis queues.  

NodeJS server example:
----------------------

    s = new rpc.Server('my_queue_name', {
      increment: function(amount) {
        return "from node server: " + (amount + 10);
      }
    });
    s.start();

NodeJS client example:
----------------------

    c = new rpc.Client('my_queue_name');
    c.invoke('increment', [12], function(err, result) {
      console.log("result is: "+result);
    });


Ruby client example:
--------------------

    client = RedPack::Client.new("my_queue_name")

    puts "making the method call asynchronously"
    client.increment_async(23) do |error, result|
      puts "async result:"
      p result
    end


Ruby server example:
--------------------

    class ExampleService
      def increment(amount)
        puts "called add_amount"
        return "from ruby: #{amount + 2}"
      end
    end

    server = RedPack::Server.new("my_queue_name")
    server.listen(ExampleService.new)


Java server example:
--------------------

    MsgpackRPCServer server = new MsgpackRPCServer("test");
    server.registerService("echo", new EchoService());
    server.start();

Java client example:
--------------------

    // Not yet ported yet!
    