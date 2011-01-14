require.paths.unshift(__dirname);

var rpc = require('../jslib/index');
var vows = require('vows');
var assert = require('assert');
var fs = require('fs');
var path = require('path');

var server = null;
var timeout = 2000;

var queueName = path.basename(__filename, '.js');

var service = {};
service.add = function(a, b) {
	return a + b;
};

service.echo = function(str) {
	return 'echo: ' + str;
};

// Setup
(function() {
	server = new rpc.Server(queueName, service);
	server.start();	
})();

// Test suite
vows.describe('RPC server tests').addBatch({
	'RPC server basic tests': {
    topic: function () {
			// Timeout to give enough time for server to start
			setTimeout(this.callback, timeout)
		},			
		
		'server should generate PID file in /tmp after started': function() {
			var pidFile = '/tmp/' + queueName + '.pid';					
			assert.isTrue(path.existsSync(pidFile));
			fs.unlinkSync(pidFile);
		},
		
		'Invoke add() via RPC client': {
			topic: function() {
				var client = new rpc.Client(queueName);
				client.invoke('add', [1, 2], this.callback);
			},
			
			'1 + 2 should be 3': function(err, result) {
				assert.equal(result, 3);
				server.kill();
			}, 
			
			'Invoke echo() via RPC client': {
				topic: function() {
					var client = new rpc.Client(queueName);
					client.invoke('echo', ['hello'], this.callback);
				},

				'should echo back - echo: hello': function(err, result) {
					assert.equal(result, 'echo: hello');
				}
			}
		}
		
	}
}).run();