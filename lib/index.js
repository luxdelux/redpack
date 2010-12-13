require.paths.unshift(__dirname);

var msgpack = require('msgpack');
var redis = require('redis');
var fs = require('fs');
var path = require('path');

const REDIS_DATA = 535; // flag to indicae the payload is for redis-msgpack rpc
const REQUEST_TYPE = 0;
const RESPONSE_TYPE = 1;
const REQ_QUEUE_PREFIX = 'redpack_request_queue:';
const RES_QUEUE_PREFIX = 'redpack_response_queue:';
const RES_QUEUE_ID_KEY = 'redpack_response_queue_index';
const BLPOP_TIMEOUT = 15; // timeout duration for server blpop

function Client(reqQueue, host, port) {
  this.reqQueue = REQ_QUEUE_PREFIX + reqQueue;
  this.count = 0;
  this.host = host;
  this.port = port;
}

Client.prototype.invoke = function(method, params, callback, timeout) {
  var self = this;
  var redisClient = redis.createClient(self.port, self.host, {return_buffers: true});
  
  var id = self.count++;
  var data = [REQUEST_TYPE, id, method, params];
//  var binaryStr = toByteArray(msgpack.pack(data));

  var req = [data, REDIS_DATA];

  if (callback !== undefined) {
    redisClient.incr(RES_QUEUE_ID_KEY, function(err, result) {
      self.resQueue = RES_QUEUE_PREFIX + result;
      self.reqSetName = self.resQueue + ':unprocessed';
      req.push(self.resQueue);
      // console.log('client pushing to ' + self.reqQueue);
      // console.log('client waiting on ' + self.resQueue);
      
      var multi = redisClient.multi();
      var msgpackData = msgpack.pack(req);
      multi.hset(self.reqSetName, id.toString(), msgpackData);
      multi.rpush(self.reqQueue, msgpackData);
      multi.exec(function() {
        self._waitForReturn(redisClient, self.resQueue, callback, timeout);
      });
    });
  } else {
    // console.log('client pushing to ' + self.reqQueue);
    // console.log('client waiting on ' + self.resQueue);    
    var msgpackData = msgpack.pack(req);
    var multi = redisClient.multi();
    multi.hset(self.reqSetName, id.toString(), msgpackData);
    multi.rpush(self.reqQueue, msgpackData);
    multi.exec(function() {
      redisClient.end();
    });
  }
};

Client.prototype._waitForReturn = function(redisClient, resQueue, callback, timeout) {
  var self = this;
  timeout = timeout || 10;
  
  redisClient.blpop(resQueue, timeout, function(err, result) {
    if (err || !result) {
      callback(err, null);
      redisClient.end();
      return;
    }

    var resMsg = msgpack.unpack(result[1]);

    if (resMsg && resMsg[1] == REDIS_DATA) {
      var res = resMsg[0];
      var error = res[2];
      var ret = res[3];
      callback(error, ret);
    }
    redisClient.end();
  });
};

Client.prototype.close = function() {
  this.redisClient.end();
};

function Server(reqQueue, service, host, port) {
  var self = this;
  
  self.pid = process.pid;

  // write the server pid to /tmp/[REQ_QUEUE_NAME]
  var fd = fs.openSync('/tmp/' + reqQueue + '.pid', 'w');
  fs.writeSync(fd, self.pid, 0);
  fs.closeSync(fd);
  
  self.service = service;
  self.reqQueue = REQ_QUEUE_PREFIX + reqQueue;
  self.host = host;
  self.port = port;
  self.killWhenReady = false;
  
  // Listen to SIGTERM signal event, which indicate the server needs to shutdown
  process.addListener('SIGTERM', function() {
    console.log('[SIGNAL] SIGTERM received, will kill server when ready.');
    self.killWhenReady = true;
  });
}

// Static method for the default monitor hook
Server.defaultMonitorHook = function(server) {
  server.redisClient.llen(server.reqQueue, function(err, result) {
    console.log('[MONITOR] ' + server.reqQueue + ' size=' + result);
  });
};

Server.prototype.setRedisTimeout = function(sec) {
  var tmpClient = redis.createClient(this.port, this.host, {return_buffers: true});
  tmpClient.sendCommand('CONFIG', 'set', 'timeout', sec, function(err, value) {
    if (err) {
      console.log(err);
    }
    tmpClient.end();
  });  
};

Server.prototype.start = function(monitorHook) {
  var self = this;
  self.monitorHook = monitorHook || Server.defaultMonitorHook;
  
  // make sure close a previous connection
  self.close();
  self.redisClient = redis.createClient(self.port, self.host, {return_buffers: true});
  _dequeue(self);
};
 
function _dequeue(server) {
  var self = server;
  
  self.redisClient.blpop(self.reqQueue, BLPOP_TIMEOUT, function(err, result) {
    if (err) {
      console.log(err);
    }
    
    if (err || !result) {
      if (self.killWhenReady) {
        console.log('server shutting down');
        self.close();
      } else {
        if (err) {
          // this is reached by err, needs to reconnect with start()
          console.log(err + '.  Restarting server.');
          self.start(self.monitorHook);
        } else {
          // both err and result is null, when BLPOP timeout.  Execute monitor hook.
          process.nextTick(function() {
            self.start(self.monitorHook);   
            self.monitorHook(self);
          });
        }
      }
      return;
    }
    
    var whole = result[1];
    var wholeMsg = msgpack.unpack(whole);
    var dataMsg = wholeMsg[0];
    var reqId = dataMsg[1];
    var method = dataMsg[2];
    var params = dataMsg[3];       

    //console.log('executing: ' + wholeMsg + ' data=' + dataMsg);
   
    var ret,res;
    try { 
      //console.log('executing ' + method);
      ret = self.service[method].apply(this, params);
      res = [RESPONSE_TYPE, reqId, null, ret];
    } catch(e) {
      res = [RESPONSE_TYPE, reqId, e, null];
    }

    if (wholeMsg.length == 3) {
      // require return data
      var resQueue = wholeMsg[2];
      // var bytes = toByteArray(msgpack.pack(res));
      
      var returnData = msgpack.pack([res, REDIS_DATA]);
      // console.log('putting return data on ' + resQueue + ' data=' + returnData);
      self.redisClient.hdel(resQueue + ':unprocessed', reqId.toString());
      self.redisClient.rpush(resQueue, returnData, function() {
        if (self.killWhenReady) {
          self.close();
        } else {
          process.nextTick(function() {
            _dequeue(self);
          });
        }
      });
    } else {
      if (self.killWhenReady) {
        self.close();
      } else {
        process.nextTick(function() {
          _dequeue(self);
        });  
      }
    }
  });
}

function toByteArray(buffer) {
  var bytes = [];
  for (i in buffer) {
    if (!isNaN(i)) {
      bytes.push(buffer[i]);
    }
  }
  return bytes;
}

Server.prototype.close = function() {
  if (this.redisClient) {
    this.redisClient.end();
  }
};

// use for testing
Server.prototype.kill = function() {
  var exec  = require('child_process').exec;
  var child = exec('kill -15 ' + this.pid, function (error, stdout, stderr) {});
};

exports.Client = Client;
exports.Server = Server;