rpc = require('../jslib/index');

var MyEchoService = {
  echo: function(param1, param2) {
    console.log("called echo, appending param: ", param1, param2);
    return "<from node: \""+param1+"\" - \""+param2+"\">"
  }
};
s = new rpc.Server('queue_name', MyEchoService);
s.start();
