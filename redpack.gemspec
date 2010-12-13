Gem::Specification.new do |s|
  s.platform = Gem::Platform::RUBY
  s.name = "redpack"
  s.version = "1.0.0"
  s.summary = "RedPack, asynchronous redis RPC library derived from MsgPack-RPC"
  s.author = "Dean Mao"
  s.email = "dean@luxdelux.com"
  s.homepage = "http://github.com/luxdelux/redpack"
  s.rubyforge_project = "redpack"
  s.has_rdoc = false
  s.require_paths = ["lib"]
  s.add_dependency "msgpack", ">= 0.4.1"
  s.add_dependency "em-redis", ">= 0.3.0"
	s.test_files = Dir["test/test_*.rb"]
  s.files = Dir["lib/**/*.rb", "test/**/*.rb"] + %w[AUTHORS LICENSE NOTICE]
end

