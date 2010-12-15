package com.luxdelux.redpack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.luxdelux.redpack.model.RPCRequest;
import com.luxdelux.redpack.model.RPCResponse;

public class RPCClient {

	public static final String REQ_QUEUE_PREFIX = "redpack_request_queue:";
	public static final String RES_QUEUE_PREFIX = "redpack_response_queue:";
	public static final String RES_QUEUE_ID_KEY = "redpack_response_queue_index";
	private static int requestCounter = 0;
	
	private RedisClient redisClient;
	private MsgpackClient msgpackClient;
	private String redisQueueName;
	private String redisResponseQueue;

	public RPCClient(String name) {
		this("localhost", name);
	}
	
	public RPCClient(String redisHost, String name) {
		this.redisQueueName = REQ_QUEUE_PREFIX + name;
		this.redisClient = new RedisClient(redisHost);
		this.msgpackClient = new MsgpackClient();
		redisClient.connect();
		this.redisResponseQueue = RES_QUEUE_PREFIX + redisClient.responseKeyName(RES_QUEUE_ID_KEY);
	}

	public Object invoke(String name, Object ... params) throws IOException, UnpackException, PackException {
		RPCRequest request = new RPCRequest(requestCounter++);
		request.setMethodName(name);
		request.setParameters(params);
		request.setResponseQueue(this.redisResponseQueue);
		redisClient.rpush(this.redisQueueName, msgpackClient.packRequest(request));
		byte[] returnVal = redisClient.blpop(request.getResponseQueue());
		
		if (returnVal == null) {
			return null;
		} else {
			RPCResponse response = msgpackClient.unpackResponse(returnVal);
			if (response.getError() != null) {
				throw new PackException(response.getError());
			}
			return response.getResult();
		}
	}
}
