require.paths.unshift(__dirname);

var mongo = require('mongodb');
var BSON = require('redpack/jslib/bson').BSON;
var redis = require('redis');
var fs = require('fs');
var path = require('path');

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

function pack(input) {
  var data = BSON.serialize(input);
  var array = [];
  for(var i = 0; i < data.length; i++) {
    array[i] = mongo.BinaryParser.toByte(data.charAt(i));
  }
  return new Buffer(array);
}

function unpack(bytes) {
  if (bytes && bytes.length > 0) {
    var serialized_data = '';
    for(var i = 0; i < bytes.length; i++) {
      serialized_data += mongo.BinaryParser.fromByte(bytes[i]);
    }
    return BSON.deserialize(serialized_data);
  } else {
    return null;
  }
}

Client.prototype.invoke = function(method, params, callback, timeout) {
  var self = this;
  if (!self.redisClient) {
    self.redisClient = redis.createClient(self.port, self.host, {return_buffers: true});
  }
  
  var id = self.count++;
  var data = [REQUEST_TYPE, id, method, params];

  var req = {"data": data};

  if (callback !== undefined) {
    self.redisClient.incr(RES_QUEUE_ID_KEY, function(err, result) {
      self.resQueue = RES_QUEUE_PREFIX + result;
      req["return"] = self.resQueue;
      var multi = self.redisClient.multi();
      var msgpackData = pack(req);
      multi.rpush(self.reqQueue, msgpackData);
      multi.exec(function() {
        self._waitForReturn(self.redisClient, self.resQueue, callback, timeout);
      });
    });
  } else {
    var msgpackData = pack(req);
    var multi = self.redisClient.multi();
    multi.rpush(self.reqQueue, msgpackData);
    multi.exec(function() {});
  }
};

Client.prototype._waitForReturn = function(redisClient, resQueue, callback, timeout) {
  var self = this;
  timeout = timeout || 10;
  
  redisClient.blpop(resQueue, timeout, function(err, result) {
    if (err || !result) {
      callback(err, null);
      return;
    }

    var resMsg = unpack(result[1]);

    if (resMsg && resMsg.data) {
      var res = resMsg.data;
      var error = res[2];
      var ret = res[3];
      callback(error, ret);
    }
  });
};

Client.prototype.close = function() {
  this.redisClient.end();
};

function Server(reqQueue, host, port) {
  var self = this;
  
  self.pid = process.pid;

  // write the server pid to /tmp/[REQ_QUEUE_NAME]
  var fd = fs.openSync('/tmp/' + reqQueue + '.pid', 'w');
  fs.writeSync(fd, self.pid, 0);
  fs.closeSync(fd);
  
  self.service = {}; // init to empty hash
  self.reqQueue = REQ_QUEUE_PREFIX + reqQueue;
  self.host = host;
  self.port = port;
  self.killWhenReady = false;
  
  self.redisClient = redis.createClient(self.port, self.host, {return_buffers: true});
  
  // Listen to SIGTERM signal event, which indicate the server needs to shutdown
  process.addListener('SIGTERM', function() {
    console.log('[SIGNAL] SIGTERM received, will kill server when ready.');
    self.killWhenReady = true;
  });
}

Server.prototype.setService = function(service) {
  this.service = service;
};

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
  self.monitorHook = monitorHook || function() {};
  self.redisClient = redis.createClient(self.port, self.host, {return_buffers: true});
  _dequeue(self);
};

Server.prototype.getReturnQueue = function() {
  return this.returnQueue;
};

Server.prototype.returnData = function(err, result) {
  var self = this;
  
  var resQueue = self.getReturnQueue();
  res = [RESPONSE_TYPE, self.reqId, err, result];
  
  self.redisClient.rpush(resQueue, pack({"data": res}), function() {
    if (self.killWhenReady) {
      self.close();
    } else {
      process.nextTick(function() {
        _dequeue(self);
      });
    }
  });  
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
            _dequeue(self);
            self.monitorHook(self);
          });
        }
      }
      return;
    }
    
    var whole = result[1];
    var wholeMsg = unpack(whole);
    var dataMsg = wholeMsg.data;
    var reqId = dataMsg[1];
    var method = dataMsg[2];
    var params = dataMsg[3];
    
    self.reqId = reqId;    
    self.returnQueue = wholeMsg.return;
    
    var ret,res;
    try {
      self.service[method].apply(self, params);
    } catch(e) {
      this.returnData(e.toString(), null);
    }

    if (!self.returnQueue) {
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

Server.prototype.close = function() {
  var self = this;
  self.redisClient.end();
  process.exit();
};

// use for testing
Server.prototype.kill = function() {
  var exec  = require('child_process').exec;
  var child = exec('kill -15 ' + this.pid, function (error, stdout, stderr) {});
};

exports.pack = pack;
exports.unpack = unpack;
exports.Client = Client;
exports.Server = Server;
