require 'msgpack'
require 'redis'
require 'em-redis'
require '../lib/redpack/base'
require '../lib/redpack/dispatcher'
require '../lib/redpack/future'
require '../lib/redpack/client'
require '../lib/redpack/exception'
require '../lib/redpack/message'
require '../lib/redpack/server'
require '../lib/redpack/transport'

client = RedPack::Client.new("blah")

puts "making the method call asynchronously"
client.increment_async(23) do |error, result|
  puts "async result:"
  p result
end

puts "making the method call synchronously"
result = client.increment_sync(44)
puts "sync result:"
p result
Kernel.exit

