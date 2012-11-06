/**
 * Copyright 2012 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.hadoop.hdfs.nfs.nfs4;

import static com.cloudera.hadoop.hdfs.nfs.nfs4.Constants.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cloudera.hadoop.hdfs.nfs.TestUtils;
import com.cloudera.hadoop.hdfs.nfs.rpc.RPCBuffer;
import com.cloudera.hadoop.hdfs.nfs.rpc.RPCRequest;
import com.cloudera.hadoop.hdfs.nfs.rpc.RPCResponse;
import com.cloudera.hadoop.hdfs.nfs.rpc.RPCTestUtil;

public class TestNFS4Server {

  NFS4Server mNFS4Server;
  int mPort;

  @After
  public void cleanup() {
    if(mNFS4Server != null) {
      try {
        mNFS4Server.shutdown();
      } catch(Exception ex) {}
    }
  }

  @Before
  public void setup() throws Exception {
    Configuration conf = TestUtils.setupConf();
    mNFS4Server = new NFS4Server();
    mNFS4Server.setConf(conf);
    mNFS4Server.start(LOCALHOST, 0);
    mPort = mNFS4Server.getPort();
  }
  @Test
  public void testNull() throws UnknownHostException, IOException {
    assertTrue(mNFS4Server.isAlive());
    Socket socket = new Socket(LOCALHOST, mPort);
    try {
      OutputStream out = socket.getOutputStream();
      InputStream in = socket.getInputStream();

      RPCBuffer buffer = new RPCBuffer();
      // save space for length
      buffer.writeInt(Integer.MAX_VALUE);
      
      RPCRequest request = RPCTestUtil.createRequest();
      request.setProcedure(NFS_PROC_NULL);
      request.write(buffer);
      buffer.flip();
      buffer.write(out);

      buffer = RPCBuffer.from(in);
      RPCResponse response = new RPCResponse();
      response.read(buffer);
      assertEquals(request.getXid(), response.getXid());
      assertEquals(RPC_REPLY_STATE_ACCEPT, response.getReplyState());
      assertEquals(RPC_ACCEPT_SUCCESS, response.getAcceptState());
    } finally {
      try {
        socket.close();
      } catch(Exception ex) {}
    }
  }
}
