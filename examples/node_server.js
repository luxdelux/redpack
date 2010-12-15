rpc = require('../jslib/index');

var MyEchoService = {
  echo: function(param) {
    console.log("called echo, appending param: "+param);
    return "<from node: \""+param+"\">"
  }
};
s = new rpc.Server('queue_name', MyEchoService);
s.start();
