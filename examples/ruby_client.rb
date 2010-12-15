#!/usr/bin/env ruby

require 'bson'
require 'redis'
require 'em-redis'
require '../rblib/redpack/base'
require '../rblib/redpack/dispatcher'
require '../rblib/redpack/future'
require '../rblib/redpack/client'
require '../rblib/redpack/exception'
require '../rblib/redpack/message'
require '../rblib/redpack/server'
require '../rblib/redpack/transport'

client = RedPack::Client.new("queue_name")
result = client.echo_sync("something")
puts "result: #{result}"
