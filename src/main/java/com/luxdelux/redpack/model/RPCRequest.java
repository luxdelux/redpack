package com.luxdelux.redpack.model;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.msgpack.MessagePackable;
import org.msgpack.MessageTypeException;
import org.msgpack.MessageUnpackable;
import org.msgpack.Packer;
import org.msgpack.Unpacker;
import org.msgpack.MessagePack;


public class RPCRequest implements MessagePackable, MessageUnpackable {

	  private static final int REQUEST_TYPE = 0;
	  private static final int VERIFICATION_CODE = 535;

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
  
	@Override
	public void messagePack(Packer packer) throws IOException {
		// needed for redpack clients
	}

	@Override
	public void messageUnpack(Unpacker unpacker) throws IOException,
			MessageTypeException {
		if (unpacker.tryUnpackNull()) {
			return;
		}
		int numElements = unpacker.unpackArray();
		unpacker.unpackArray();
		this.setType(unpacker.unpackInt());
		this.setRequestId(unpacker.unpackInt());
		this.setMethodName(unpacker.unpackString());

		Object[] parameters = new Object[unpacker.unpackArray()];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = unpacker.unpackObject();

			if (parameters[i] instanceof byte[]) {
				byte[] bytes = (byte[]) parameters[i];
				parameters[i] = new String(bytes, Charset.forName("UTF-8"));
			} else if (parameters[i] instanceof Byte
					|| parameters[i] instanceof Short
					|| parameters[i] instanceof Integer
					|| parameters[i] instanceof Long) {
				parameters[i] = new Long(((Number) parameters[i]).longValue());
			} else {
				// TODO Handle floating point numbers (java.lang.Float,
				// java.lang.Double)
				// TODO Handle lists (java.util.Arrays.ArrayList)
				// TODO Handle maps (java.util.HashMap)
			}
		}
		this.setParameters(parameters);
		this.setVerificationCode(unpacker.unpackInt());
		if (numElements > 2) {
			this.setResponseQueue(unpacker.unpackString());
		}
	}
}
