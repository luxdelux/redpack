module RedPack

REQUEST  = 0    # [0, msgid, method, param]
RESPONSE = 1    # [1, msgid, error, result]
NOTIFY   = 2    # [2, method, param]

NO_METHOD_ERROR = 0x01;
ARGUMENT_ERROR  = 0x02;

REDIS_DATA = 535 # == 'redis'.split('').inject(0) {|sum,x| sum+=x.ord} -- I know this is lame, but had to think of something!

end
