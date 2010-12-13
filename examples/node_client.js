rpc = require('../lib/index');

// c = new rpc.Client('blah');
// c.invoke('increment', [1], function(err, result) {
//   console.log("result is:");
//   console.dir(result);
// });

c = new rpc.Client('test');
c.invoke('echo', ["something"], function(err, result) {
  console.log("result is:");
  console.dir(result);
});
