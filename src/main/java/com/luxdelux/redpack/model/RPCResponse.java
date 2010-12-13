package com.luxdelux.redpack.model;

import java.io.IOException;
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

	@Override
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

	@Override
	public void messageUnpack(Unpacker unpacker) throws IOException,
			MessageTypeException {
		// needed for redpack clients
	}
}
