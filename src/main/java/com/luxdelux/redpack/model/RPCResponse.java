package com.luxdelux.redpack.model;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.msgpack.MessagePackable;
import org.msgpack.MessageTypeException;
import org.msgpack.MessageUnpackable;
import org.msgpack.Packer;
import org.msgpack.Unpacker;

public class RPCResponse implements MessagePackable, MessageUnpackable {

	private static final int RESPONSE_TYPE = 1;
	private static final int VERIFICATION_CODE = 535;

	private int requestId;
	private Object result;
	private String error;
	
	public RPCResponse() {
		
	}

	public RPCResponse(RPCRequest request, Object result, String error) {
		this.error = error;
		this.result = result;
		this.requestId = request.getRequestId();
	}

	public int getRequestId() {
		return requestId;
	}

	public Object getResult() {
		return result;
	}

	public String getError() {
		return error;
	}

	public void messagePack(Packer packer) throws IOException {
		ArrayList outList = new ArrayList();
		ArrayList inList = new ArrayList();
		inList.add(RESPONSE_TYPE);
		inList.add(this.getRequestId());
		inList.add(this.getError());
		inList.add(this.getResult());
		outList.add(inList);
		outList.add(VERIFICATION_CODE);

		try {
			packer.pack(outList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void messageUnpack(Unpacker unpacker) throws IOException,
			MessageTypeException {
		if (unpacker.tryUnpackNull()) {
			return;
		}
		int numElements = unpacker.unpackArray();
		unpacker.unpackArray();
		if (unpacker.unpackInt() == RESPONSE_TYPE) {
			this.requestId = unpacker.unpackInt();
			this.error = unpacker.unpackString();
			this.result = unpacker.unpackObject();
		}
	}
}
