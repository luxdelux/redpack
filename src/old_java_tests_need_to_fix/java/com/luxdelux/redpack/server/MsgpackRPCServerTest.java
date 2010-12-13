package com.luxdelux.redpack.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.luxdelux.redpack.server.MsgpackRPCServer;

public class MsgpackRPCServerTest {

  private File pidFile;
  private MsgpackRPCServer server;

  @Before
  public void initializeServerAndDeleteExistingPIDFile() {
    server = new MsgpackRPCServer("test");
    pidFile = new File(server.getPIDFileName());

    if (pidFile.exists()) {
      pidFile.delete();
    }
  }

  @After
  public void shutdownServer() throws InterruptedException {
    // A java.util.concurrent.RejectedExecutionException is expected if the
    // server is stopped immediately after being started because the server's
    // spawned thread has not had an opportunity to execute any tasks; to avoid
    // this, sleep the current thread for a short period of time, although this
    // will negatively impact the running time of all tests.
    server.stop();
  }

  @Test
  public void pidFileIsWrittenOnServerStart() {
    assertFalse("PID file should not exist before server starts", pidFile.exists());
    server.start();
    assertTrue("PID file should exist after server starts", pidFile.exists());
  }

  @Test
  public void pidFileIsDeletedOnServerShutdown() throws InterruptedException {
    server.start();
    assertTrue("PID file should exist before server shuts down", pidFile.exists());
    server.stop();
    assertFalse("PID file should not exist after server shuts down", pidFile.exists());
  }

  @Test
  public void pidFileIsNotEmpty() {
    server.start();
    if (!pidFile.exists()) {
      fail("PID file should exist after server starts");
    }

    assertTrue("PID file should not be empty", pidFile.length() > 0);
  }

  @Test
  public void pidFileContainsValidPID() {
    server.start();
    if (!pidFile.exists()) {
      fail("PID file should exist after server starts");
    }

    String fileContent = getFileContents(pidFile);
    assertTrue("PID file should contain numeric string", fileContent.matches("^\\d+$"));
  }

  private static String getFileContents(File file) {
    StringBuilder fileContents = new StringBuilder();
    try {
      BufferedReader input =  new BufferedReader(new FileReader(file));
      try {
        String line = null;
        while ((line = input.readLine()) != null) {
          fileContents.append(line);
        }
      } finally {
        input.close();
      }
    } catch (IOException e) {
      return null;
    }

    return fileContents.toString();
  }
}
