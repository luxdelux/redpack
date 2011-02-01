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
client.echo_async("something", 722) do |err, result|
  p "async called #{result}"
end

sleep(1)
