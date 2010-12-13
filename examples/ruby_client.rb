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

client = RedPack::Client.new("queue_name")
result = client.echo_sync("something")
puts "result: #{result}"
