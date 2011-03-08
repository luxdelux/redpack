rpc = require('../jslib/index');

c = new rpc.Client('queue_name');
c.invoke('echo', [424, "something"], function(err, result) {
  console.log("result is:\n"+result);
});

c = new rpc.Client('queue_name');
c.invoke('test', ['hello'], function(err, result) {
  if (err) {
    console.log(err);
    return;
  }
  
  console.log("result is:\n"+result);
});

c = new rpc.Client('queue_name');
c.invoke('foo', ['hello']);