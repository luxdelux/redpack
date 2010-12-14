package com.luxdelux.redpack.model;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;

public class RPCResponse {
	private static final int RESPONSE_TYPE = 1;

	private int requestId;
	private Object result;
	private String error;
	
	public RPCResponse(RPCRequest request, Object result, String error) {
		this.error = error;
		this.result = result;
		this.requestId = request.getRequestId();
	}
	
	public RPCResponse(BSONObject obj) {
		  BasicBSONList list = (BasicBSONList) obj.get("data");
		  this.requestId = (Integer) list.get(1);
		  this.result = list.get(2);
		  this.error = list.get(3).toString();
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
	
	  public BSONObject getBSONObject() {
		  BSONObject obj = new BasicBSONObject();
		  BasicBSONList list = new BasicBSONList();
		  list.add(RESPONSE_TYPE);
		  list.add(this.error);
		  list.add(this.result);
		  return obj;
	  }
}
