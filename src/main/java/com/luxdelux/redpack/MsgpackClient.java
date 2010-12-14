package com.luxdelux.redpack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.msgpack.MessageTypeException;
import org.msgpack.Packer;
import org.msgpack.Unpacker;

import com.luxdelux.redpack.model.RPCRequest;
import com.luxdelux.redpack.model.RPCResponse;

public class MsgpackClient {

  private Unpacker unpacker;

  public MsgpackClient() {
    unpacker = new Unpacker();
  }

  public byte[] packResponse(RPCResponse response) throws PackException, IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    Packer packer = new Packer(out);
	    response.messagePack(packer);
	    return out.toByteArray();
  }

  public RPCResponse unpackResponse(byte[] packed) throws UnpackException, MessageTypeException, IOException {
	    RPCResponse response = new RPCResponse();
	    unpacker.reset();
	    unpacker.feed(packed);
	    response.messageUnpack(unpacker);
	    return response;
	  }

  public RPCRequest unpackRequest(byte[] packed) throws UnpackException, MessageTypeException, IOException {
    RPCRequest request = new RPCRequest();
    unpacker.reset();
    unpacker.feed(packed);
    request.messageUnpack(unpacker);
    return request;
  }
}
