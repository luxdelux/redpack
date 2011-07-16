var mongo = require('mongodb');
var BinaryParser = mongo.BinaryParser;
var bsonpure = mongo.BSONPure;
var BSON = bsonpure.BSON;

var Integer = require('mongodb/lib/mongodb/goog/math/integer').Integer;

// This modified version of BSON does not differentiate between integers & longs, it will always use the javascript Number type

with(bsonpure) {
  BSON.encodeValue = function(encoded_string, variable, value, top_level, checkKeys) {
    var variable_encoded = variable == null ? '' : BinaryParser.encode_cstring(variable);
    if(checkKeys && variable != null)BSON.checkKey(variable);

    if(value == null) {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_NULL) + variable_encoded;
    } else if(value.constructor == String) {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_STRING) + variable_encoded + BSON.encodeString(value);
    } else if(value instanceof Timestamp || Object.prototype.toString.call(value) == "[object Timestamp]") {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_TIMESTAMP) + variable_encoded + BSON.encodeLong(value);
    } else if(value instanceof Long || Object.prototype.toString.call(value) == "[object Long]") {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_LONG) + variable_encoded + BSON.encodeLong(value);
    } else if(value.constructor == Number) {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_NUMBER) + variable_encoded + BSON.encodeFloat(value);
    } else if(Array.isArray(value)) {
      var object_string = BSON.encodeArray(value, checkKeys);
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_ARRAY) + variable_encoded + BSON.encodeInt(Integer.fromInt(object_string.length + 4 + 1)) + object_string + BinaryParser.fromByte(0);
    } else if(Object.prototype.toString.call(value) === '[object Boolean]') {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_BOOLEAN) + variable_encoded + BSON.encodeBoolean(value);
    } else if(Object.prototype.toString.call(value) === '[object Date]') {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_DATE) + variable_encoded + BSON.encodeDate(value);
    } else if(Object.prototype.toString.call(value) === '[object RegExp]') {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_REGEXP) + variable_encoded + BSON.encodeRegExp(value);
    } else if(value instanceof ObjectID || (value.id && value.toHexString) || Object.prototype.toString.call(value) == "[object ObjectID]") {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_OID) + variable_encoded + BSON.encodeOid(value);
    } else if(value instanceof Code || Object.prototype.toString.call(value) == "[object Code]") {
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_CODE_W_SCOPE) + variable_encoded + BSON.encodeCode(value, checkKeys);
    } else if(value instanceof Binary || Object.prototype.toString.call(value) == "[object Binary]") {
      var object_string = BSON.encodeBinary(value);
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_BINARY) + variable_encoded + BSON.encodeInt(Integer.fromInt(object_string.length - 1)) + object_string;
    } else if(value instanceof DBRef) {
      var object_string = BSON.encodeDBRef(value);
      encoded_string += BinaryParser.fromByte(BSON.BSON_DATA_OBJECT) + variable_encoded + BSON.encodeInt(Integer.fromInt(object_string.length + 4 + 1)) + object_string + BinaryParser.fromByte(0);
    } else if(Object.prototype.toString.call(value) === '[object Object]') {
      var object_string = BSON.encodeObject(value, checkKeys);
      encoded_string += (!top_level ? BinaryParser.fromByte(BSON.BSON_DATA_OBJECT) : '') + variable_encoded + BSON.encodeInt(Integer.fromInt(object_string.length + 4 + 1)) + object_string + BinaryParser.fromByte(0);
    }

    return encoded_string;
  };  
}

exports.BSON = BSON;