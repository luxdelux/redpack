rpc = require('../jslib/index');

c = new rpc.Client('queue_name');
c.invoke('echo', [424, "something"], function(err, result) {
  console.log("result is:\n"+result);
});
