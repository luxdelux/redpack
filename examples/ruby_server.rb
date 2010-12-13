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

class Blah
  def increment(amount)
    puts "called add_amount, sleeping 2 second"
    sleep(2)
    "from ruby: #{amount + 2}"
  end
end

server = RedPack::Server.new("blah")
server.listen(Blah.new)

