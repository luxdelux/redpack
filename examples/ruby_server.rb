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

class MyEchoService
  def echo(param)
    puts "called echo"
    "<from ruby: \"#{param}\">"
  end
end

RedPack::Server.new("queue_name").listen(MyEchoService.new)

