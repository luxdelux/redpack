#
# RedPack-RPC for Ruby modified from MessagePack-RPC
#
# Copyright (C) 2010 FURUHASHI Sadayuki
# Copyright (C) 2010 Lux Delux Inc
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#
module RedPack

# Server is usable for RPC server.
class Server
  def initialize(name, obj, redis_options = {:host => 'localhost'})
    @dispatcher = nil
    @obj = obj
    @listener = RedisServerTransport.new(name, redis_options)
  end
  
	def serve(obj, accept = obj.public_methods)
		@dispatcher = ObjectDispatcher.new(obj, accept)
		self
	end

	# 1. listen(listener, obj = nil, accept = obj.public_methods)
	# 2. listen(host, port, obj = nil, accept = obj.public_methods)
	def start()
		unless @obj.nil?
			serve(@obj, @obj.public_methods)
		end

		@listener.listen(self)
		nil
	end

	def close
	  @listener.close
	end

	# from ServerTransport
	def on_request(sendable, msgid, method, param)  #:nodoc:
		responder = Responder.new(sendable, msgid)
		@dispatcher.dispatch_request(self, method, param, responder)
	end

	# from ServerTransport
	def on_notify(method, param)  #:nodoc:
		@dispatcher.dispatch_notify(self, method, param)
	end
end


class Responder
	def initialize(sendable, msgid)
		@sendable = sendable  # send_message method is required
		@msgid = msgid
		@sent = false
	end

	def sent?
		@sent
	end

	def result(retval, err = nil)
		unless @sent
			data = [RESPONSE, @msgid, err, retval]
			@sendable.send_data(data)
  		@sendable.finish(@msgid)
			@sent = true
		end
		nil
	end

	def error(err, retval = nil)
		result(retval, err)
	end
end

end
