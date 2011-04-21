Gem::Specification.new do |s|
  s.platform = Gem::Platform::RUBY
  s.name = "redpack"
  s.version = "1.0.6"
  s.description = "Simple Scalable RPC using Redis & BSON"
  s.summary = "RedPack, asynchronous redis RPC library derived from MsgPack-RPC using BSON serialization"
  s.author = "Dean Mao"
  s.email = "deanmao@gmail.com"
  s.homepage = "http://github.com/luxdelux/redpack"
  s.rubyforge_project = "redpack"
  s.has_rdoc = false
  s.require_paths = ["rblib"]
  s.add_dependency "bson", ">= 1.1.4"
  s.add_dependency "em-redis", ">= 0.3.0"
	s.test_files = Dir["test/test_*.rb"]
  s.files = Dir["rblib/**/*.rb", "test/**/*.rb"] + %w[AUTHORS LICENSE NOTICE]
end

