package com.luxdelux.redpack.model;

import java.util.Arrays;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;

public class RPCRequest {
	private static final int REQUEST_TYPE = 0;

	private int type;
	private int requestId;
	private int verificationCode;
	private String methodName;
	private String responseQueue;
	private Object[] parameters;

	public RPCRequest() {
	}

	public RPCRequest(int requestId) {
		this.requestId = requestId;
	}

	public RPCRequest(BSONObject obj) {
		BasicBSONList list = (BasicBSONList) obj.get("data");
		this.requestId = (Integer) list.get(1);
		this.methodName = list.get(2).toString();
		this.parameters = ((BasicBSONList) list.get(3)).toArray();
		this.responseQueue = obj.get("return").toString();
	}

	public int getType() {
		return type;
	}

	public int getRequestId() {
		return requestId;
	}

	public int getVerificationCode() {
		return verificationCode;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getResponseQueue() {
		return responseQueue;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public void setVerificationCode(int verificationCode) {
		this.verificationCode = verificationCode;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setResponseQueue(String responseQueue) {
		this.responseQueue = responseQueue;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public BSONObject getBSONObject() {
		BSONObject obj = new BasicBSONObject();
		BasicBSONList list = new BasicBSONList();
		list.add(REQUEST_TYPE);
		list.add(this.requestId);
		list.add(this.methodName);
		BasicBSONList paramList = new BasicBSONList();
		paramList.addAll(Arrays.asList(parameters));
		list.add(paramList);
		obj.put("data", list);
		obj.put("return", this.responseQueue);
		return obj;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + Arrays.hashCode(parameters);
		result = prime * result + requestId;
		result = prime * result
				+ ((responseQueue == null) ? 0 : responseQueue.hashCode());
		result = prime * result + type;
		result = prime * result + verificationCode;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		RPCRequest other = (RPCRequest) obj;
		if (methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!methodName.equals(other.methodName)) {
			return false;
		}
		if (!Arrays.equals(parameters, other.parameters)) {
			return false;
		}
		if (requestId != other.requestId) {
			return false;
		}
		if (responseQueue == null) {
			if (other.responseQueue != null) {
				return false;
			}
		} else if (!responseQueue.equals(other.responseQueue)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (verificationCode != other.verificationCode) {
			return false;
		}
		return true;
	}
}
