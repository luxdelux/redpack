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

class Client
	def initialize(name, redis_options = {:host => 'localhost'})
		@timeout = 10
		@seqid = 0
		@reqtable = {}
		@transport = RedisClientTransport.new(self, redis_options, name)
	end
	attr_accessor :timeout

	# call-seq:
	#   notify(symbol, *args) -> nil
	#
	# Calls remote method with NOTIFY protocol.
	# It doesn't require server to return results.
	# This method is non-blocking and returns nil.
	def notify(method, *args)
		send_notify(method, args)
		nil
	end
	
	# call code like this for sync calls:
	#   counter = client.increment_remote_counter_sync(1, 2, 3)
	# 
	# call code like this for async non-blocking calls:
	#   client.increment_remote_counter_async(1, 2, 3) do |error, result|
	#     if error
	#       raise "we got an error"
	#     else
	#       counter = result
	#     end
	#   end
	def method_missing(method, *args, &block)
	  method_name = method.to_s
	  sync = method_name.end_with?("_sync")
	  async = method_name.end_with?("_async")
    if sync || async
	    method_name.gsub!(/_sync$/, '') if sync
	    method_name.gsub!(/_async$/, '') if async
	    future = send_request(method_name, args)
  		future.attach_callback(block) if block
  		if sync
  		  @transport.listen_for_return_sync
  		  future.get
  		  if future.error
  		    raise error
		    else
		      return future.result
	      end
	    elsif async
  		  @transport.listen_for_return_async
	      return future
      end
    else
      super
    end
  end
  

	# Closes underlaying Transport and destroy resources.
	def close
	  @timer.detach if @timer.attached?
	  @reqtable = {}
		@transport.close
		@seqid = 0
		self
	end
	
	# from ClientTransport
	def on_response(sock, msgid, error, result)  #:nodoc:
		if future = @reqtable.delete(msgid)
			future.set_result(error, result)
		end
	end

	# from ClientTransport
	def on_connect_failed  #:nodoc:
		@reqtable.reject! {|msgid, future|
			begin
				future.set_result ConnectError.new, nil
			rescue
			end
			true
		}
		nil
	end

	# from Client, SessionPool
	def step_timeout  #:nodoc:
		timedout = []
		@reqtable.reject! {|msgid, future|
			if future.step_timeout
				timedout.push(future)
				true
			end
		}
		timedout.each {|future|
			begin
				future.set_result TimeoutError.new, nil
			rescue
			end
		}
		!@reqtable.empty?
	end


	private
	def send_request(method, param)
		method = method.to_s
		msgid = @seqid
		@seqid += 1; if @seqid >= 1<<31 then @seqid = 0 end
		data = [REQUEST, msgid, method, param]
		@transport.send_data(data, msgid)
		@reqtable[msgid] = Future.new(self)
	end

	def send_notify(method, param)
		method = method.to_s
		data = [NOTIFY, method, param]
		@transport.send_data(data, nil)
		nil
	end
end

end
