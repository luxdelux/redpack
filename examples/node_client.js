rpc = require('../jslib/index');

c = new rpc.Client('queue_name');
c.invoke('echo', ["something"], function(err, result) {
  console.log("result is:\n"+result);
});
