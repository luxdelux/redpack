RedPack
=============

Super simple RPC using a slightly modified form of MsgPack-RPC, on top of Redis queues.  

NodeJS example:
---------------

    s = new rpc.Server('my_queue_name', {
      increment: function(amount) {
        return "from node: " + (amount + 10);
      }
    });
    s.start();
