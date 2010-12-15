package com.luxdelux.redpack;

import java.io.IOException;

import org.bson.BSON;
import org.bson.BSONObject;

import com.luxdelux.redpack.model.RPCRequest;
import com.luxdelux.redpack.model.RPCResponse;

public class MsgpackClient {

  public MsgpackClient() {
  }
  
  public byte[] packRequest(RPCRequest request) {
	  return BSON.encode(request.getBSONObject());
  }

  public byte[] packResponse(RPCResponse response) throws PackException, IOException {
	  return BSON.encode(response.getBSONObject());
  }

  public RPCResponse unpackResponse(byte[] packed) throws UnpackException, IOException {
	  BSONObject obj = BSON.decode(packed);
	  return new RPCResponse(obj);
	  }

  public RPCRequest unpackRequest(byte[] packed) throws UnpackException, IOException {
	  BSONObject obj = BSON.decode(packed);
	  return new RPCRequest(obj);
  }
}
