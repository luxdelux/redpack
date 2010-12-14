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


# Future describes result of remote procedure call that is initially not known,
# because it is not yet received.
# You can wait and get the result with get method.
class Future
	def initialize(session, callback = nil)  #:nodoc:
		@timeout = session.timeout
		@callback_handler = callback
		@error_handler = nil
		@result_handler = nil
		@set = false
		@error = nil
		@result = nil
	end
	attr_accessor :result, :error

	# Wait for receiving result of remote procedure call and returns its result.
	# If the remote method raises error, then this method raises RemoteError.
	# If the remote procedure call failed with timeout, this method raises TimeoutError.
	# Otherwise this method returns the result of remote method.
	def get
		join
		if error.nil?
			if @result_handler
				return @result_handler.call(@result)
			else
				return @result
			end
		end
		if @error_handler
			return @error_handler.call(self)
		else
			raise @error if @error.is_a?(Error)
			raise RemoteError.new(@error, @result)
		end
	end

	# Wait for receiving result of remote procedure call.
	# This method returns self.
	# If a callback method is attached, it will be called.
	def join
		until @set
		  sleep(0.0001)
		end
		self
	end

	# call-seq:
	#   attach_callback {|future|  }
	#
	# Attaches a callback method that is called when the result of remote method is received.
	def attach_callback(proc = nil, &block)
		@callback_handler = proc || block
	end

	# For IDL
	def attach_error_handler(proc = nil, &block)  #:nodoc:
		@error_handler = proc || block
	end

	# For IDL
	def attach_result_handler(proc = nil, &block)  #:nodoc:
		@result_handler = proc || block
	end

	def set_result(err, res)  #:nodoc:
		@error  = err
		@result = res
		if @callback_handler
			if @callback_handler.arity == 2
				# FIXME backward compatibility
				@callback_handler.call(error, result)
			else
				@callback_handler.call(self)
			end
		end
		@set = true
	end
end

end
