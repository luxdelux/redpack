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

module Util
  # convert a binary sring into an array of bytes
  def to_bytes(binary_string)
    binary_string.unpack('c*')
  end

  # convert an array of bytes into a binary string
  def to_binary_string(bytes)
    bytes.pack('c*')
  end
end

class RedisClientTransport
  include Util
  attr_reader :identifier

  # Pass in {:ignore_return_value => true} as part of the redis_options so that the server doesn't put the return
  # object in a queue.  This is useful if you're doing call_async and never care about the return value.  It saves
  # Redis from adding a key/list pair.  A synchronous call will result in an immediate timeout exception.
	def initialize(client, redis_options, name)
		@client = client
		@queue_name = "redpack_request_queue:#{name}"
    @ignore_return_value = !!redis_options.delete(:ignore_return_value)
    if @ignore_return_value
      @client.timeout = 0
    else
      @client.timeout = 15
    end

    @redis_options = redis_options
    @redis = Redis.new(redis_options)

		# assign a unique client key for this instance which will be used for return values
		@identifier = @redis.incr("redpack_response_queue_index")
		@return_queue_name = "redpack_response_queue:#{@identifier}"
		@unprocessed_requests_name = "#{@return_queue_name}:unprocessed"
		@pool = 0
	end

  def process_data(data)
    # puts "done waiting for #{@return_queue_name}"
    if data && data[1]
      begin
        redis_packet = MessagePack.unpack(data[1])
        if redis_packet[1] == REDIS_DATA
          msg = redis_packet[0]
          if msg[0] == RESPONSE
            on_response(msg[1], msg[2], msg[3])
          else
            puts "unknown message type #{msg[0]}"
          end
        end
      rescue => e
        puts e
      end
    end
  end

	def listen_for_return_sync
    process_data(@redis.blpop(@return_queue_name, 0))
  end

	def listen_for_return_async
	  Thread.new do
      unless @ignore_return_value
		    EM.run do
  		    redis = EventMachine::Protocols::Redis.connect(@redis_options)
  		    # puts "waiting for #{@return_queue_name}"
  		    redis.blpop(@return_queue_name, 0) do |data|
  		      process_data(data)
  	      end
  	    end
  	  end
    end
  end

	def redis_push(msgpack_data, msgid = nil)
	  if msgid
      @redis.multi
      # puts "setting key in #{@unprocessed_requests_name}"
      @redis.hset(@unprocessed_requests_name, msgid.to_s, msgpack_data)
      # puts "pushing item into #{@queue_name}"
      @redis.rpush(@queue_name, msgpack_data)
      @redis.exec
    else
      @redis.rpush(@queue_name, msgpack_data)
    end
  end

	def send_data(data, msgid = nil)
	  if @ignore_return_value
	    redis_push([data, REDIS_DATA].to_msgpack, msgid)
    else
	    redis_push([data, REDIS_DATA, @return_queue_name].to_msgpack, msgid)
    end
	end

	def close
		self
	end

	def on_connect(sock)
	end

	def on_response(msgid, error, result)
		@client.on_response(self, msgid, error, result)
	end

	def on_connect_failed(sock)
	end

	def on_close(sock)
	end
end

class RedisServerTransport
  include Util
  
	def initialize(name, redis_options = {})
	  @ignore_nil_returns = !!redis_options.delete(:ignore_nil_returns)
    @redis = Redis.new(redis_options)
		@queue_name = "redpack_request_queue:#{name}"
		@timeout_mechanism = redis_options[:timeout_mechanism]
	end

	# ServerTransport interface
	def listen(server)
		@server = server
    loop do
      begin
        # puts "listening to #{@queue_name}"
        data = @redis.blpop(@queue_name, 0)
        # puts "popped item off of #{@queue_name}"
    		if data && data[1]
    		  redis_packet = MessagePack.unpack(data[1])
    		  if redis_packet[1] == REDIS_DATA
    		    @return_queue_name = redis_packet[2]
    		    @unprocessed_requests_name = "#{@return_queue_name}:unprocessed"
    		    msg = redis_packet[0]
      		  case msg[0]
        		when REQUEST
        			@server.on_request(self, msg[1], msg[2], msg[3])
        		when RESPONSE
        		  puts "response message on server session"
        		when NOTIFY
        			@server.on_notify(msg[1], msg[2])
        		else
        			puts "unknown message type #{msg[0]}"
        		end
  		    end
    		end
    	rescue => e
    	  # probably timed out
    	  p e
    	  @timeout_mechanism.call if @timeout_mechanism
		  end
    end
	end
	
	def finish(msgid)
	  # puts "removing key from #{@unprocessed_requests_name}"
	  @redis.hdel(@unprocessed_requests_name, msgid.to_s)
  end

	def send_data(data)
	  # puts "putting data on #{@return_queue_name}"
    @redis.rpush(@return_queue_name, [data, REDIS_DATA].to_msgpack) if @return_queue_name
  end

	# ServerTransport interface
	def close
	end
end

end