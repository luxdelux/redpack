rpc = require('../jslib/index');

var server = new rpc.Server('queue_name');

var MyService = {
  echo: function(param1, param2) {
    console.log("called echo, appending param: ", param1, param2);
    server.returnData(null, "<from node: \""+param1+"\" - \""+param2+"\">");
  },
  
  test: function(data) {
    setTimeout(function() {
      server.returnData('error', 'austin chau');
    }, 2000);
  },
  
  foo: function(data) {
    console.log(data);
  }
};
server.setService(MyService);
server.start();
