rpc = require('../lib/index');

s = new rpc.Server('blah', {
  increment: function(amount) {
    console.log("called increment, sleeping for 2 seconds");
    var endTime = new Date().getTime() + 2000;
    while(new Date().getTime() < endTime) {
      ;
    }
    return "from node: " + (amount + 10);
  }
});
s.start();