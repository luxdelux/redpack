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

class MyEchoService
  def echo(param1, param2)
    puts "called echo"
    "<from ruby: \"#{param1}\" - \"#{param2}\">"
  end
end

RedPack::Server.new("queue_name", MyEchoService.new).start()

