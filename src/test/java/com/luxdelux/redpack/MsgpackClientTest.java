package com.luxdelux.redpack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.luxdelux.redpack.model.RPCRequest;
import com.luxdelux.redpack.model.RPCResponse;

public class MsgpackClientTest {

  @Test
  public void unpackSynchronousRequestWithOneStringParameter() throws UnpackException {
    // Parameter 1: "149128395122456%7Cfaf164c4425a70b31e4a1952-3400228%7C8g8Hvf1uXBGrdhQf2b6wkJOr6pE"
    int[] bytes = {147, 220, 0, 97, 208, 148, 0, 0, 208, 169, 112, 111, 108, 121, 112, 104, 111, 110, 121, 208, 145, 208, 218, 0, 80, 49, 52, 57, 49, 50, 56, 51, 57, 53, 49, 50, 50, 52, 53, 54, 37, 55, 67, 102, 97, 102, 49, 54, 52, 99, 52, 52, 50, 53, 97, 55, 48, 98, 51, 49, 101, 52, 97, 49, 57, 53, 50, 45, 51, 52, 48, 48, 50, 50, 56, 37, 55, 67, 56, 103, 56, 72, 118, 102, 49, 117, 88, 66, 71, 114, 100, 104, 81, 102, 50, 98, 54, 119, 107, 74, 79, 114, 54, 112, 69, 205, 2, 23, 185, 114, 112, 99, 45, 114, 101, 116, 117, 114, 110, 45, 105, 100, 101, 110, 116, 105, 102, 105, 101, 114, 45, 49, 49, 50};
    Object[] parameters = {"149128395122456%7Cfaf164c4425a70b31e4a1952-3400228%7C8g8Hvf1uXBGrdhQf2b6wkJOr6pE"};

    compareSynchronousRequests(parameters, bytes);
  }

  @Test
  public void unpackAsynchronousRequestWithOneStringParameter() throws UnpackException {
    // Parameter 1: "149128395122456%7Cfaf164c4425a70b31e4a1952-3400228%7C8g8Hvf1uXBGrdhQf2b6wkJOr6pE"
    int[] bytes = {146, 220, 0, 97, 208, 148, 0, 0, 208, 169, 112, 111, 108, 121, 112, 104, 111, 110, 121, 208, 145, 208, 218, 0, 80, 49, 52, 57, 49, 50, 56, 51, 57, 53, 49, 50, 50, 52, 53, 54, 37, 55, 67, 102, 97, 102, 49, 54, 52, 99, 52, 52, 50, 53, 97, 55, 48, 98, 51, 49, 101, 52, 97, 49, 57, 53, 50, 45, 51, 52, 48, 48, 50, 50, 56, 37, 55, 67, 56, 103, 56, 72, 118, 102, 49, 117, 88, 66, 71, 114, 100, 104, 81, 102, 50, 98, 54, 119, 107, 74, 79, 114, 54, 112, 69, 205, 2, 23};
    Object[] parameters = {"149128395122456%7Cfaf164c4425a70b31e4a1952-3400228%7C8g8Hvf1uXBGrdhQf2b6wkJOr6pE"};

    compareAsynchronousRequests(parameters, bytes);
  }

  @Test
  public void unpackSynchronousRequestWithTwoLongParameters() throws UnpackException {
    // Parameter 1: 50232
    // Parameter 2: 1093923
    int[] bytes = {147, 220, 0, 22, 208, 148, 0, 0, 208, 169, 112, 111, 108, 121, 112, 104, 111, 110, 121, 208, 146, 208, 205, 208, 196, 56, 208, 206, 0, 16, 208, 177, 35, 205, 2, 23, 185, 114, 112, 99, 45, 114, 101, 116, 117, 114, 110, 45, 105, 100, 101, 110, 116, 105, 102, 105, 101, 114, 45, 49, 49, 50};
    Object[] parameters = {new Long(50232), new Long(1093923)};

    compareSynchronousRequests(parameters, bytes);
  }

  @Test
  public void unpackAsynchronousRequestWithTwoLongParameters() throws UnpackException {
    // Parameter 1: 50232
    // Parameter 2: 1093923
    int[] bytes = {146, 220, 0, 22, 208, 148, 0, 0, 208, 169, 112, 111, 108, 121, 112, 104, 111, 110, 121, 208, 146, 208, 205, 208, 196, 56, 208, 206, 0, 16, 208, 177, 35, 205, 2, 23};
    Object[] parameters = {new Long(50232), new Long(1093923)};

    compareAsynchronousRequests(parameters, bytes);
  }

  @Test
  public void packStringResponse() throws PackException {
    RPCResponse responseObj = new RPCResponse(new RPCRequest(0), "200 OK", null);
    MsgpackClient client = new MsgpackClient();

    byte[] expectedResponseBytes = intArrToByteArr(new int[] {146, 155, 204, 148, 1, 0, 204, 192, 204, 166, 50, 48, 48, 32, 79, 75, 205, 2, 23});
    byte[] actualResponseBytes = client.packResponse(responseObj);

    assertTrue(Arrays.equals(actualResponseBytes, expectedResponseBytes));
  }

  private void compareSynchronousRequests(Object[] parameters, int[] bytes) throws UnpackException {
    MsgpackClient client = new MsgpackClient();

    RPCRequest expectedRequestObj = getRPCRequestFixture("rpc-return-identifier-112", parameters);
    RPCRequest actualRequestObj = client.unpackRequest(bytes);
    assertEquals(actualRequestObj, expectedRequestObj);
  }

  private void compareAsynchronousRequests(Object[] parameters, int[] bytes) throws UnpackException {
    MsgpackClient client = new MsgpackClient();

    RPCRequest expectedRequestObj = getRPCRequestFixture(null, parameters);
    RPCRequest actualRequestObj = client.unpackRequest(bytes);
    assertEquals(actualRequestObj, expectedRequestObj);
  }

  // Pass in null for responseQueueName for asynchronous requests
  private RPCRequest getRPCRequestFixture(String responseQueueName, Object[] parameters) {
    RPCRequest requestObj = new RPCRequest();
    requestObj.setType(0);
    requestObj.setRequestId(0);
    requestObj.setVerificationCode(535);
    requestObj.setParameters(parameters);
    requestObj.setMethodName("polyphony");
    requestObj.setResponseQueue(responseQueueName);
    return requestObj;
  }

  private static byte[] intArrToByteArr(int[] arr) {
    byte[] ret = new byte[arr.length];
    for (int i = 0; i < arr.length; i++) {
      ret[i] = (byte) arr[i];
    }

    return ret;
  }
}
