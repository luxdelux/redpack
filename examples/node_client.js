rpc = require('../jslib/index');

c = new rpc.Client('queue_name');
c.invoke('echo', ["something", 722], function(err, result) {
  console.log("result is:\n"+result);
});
