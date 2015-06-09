/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.nsdchat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatConnection {

    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;

    private static final String TAG = "ChatConnection";

    private Socket mSocket;
    private int mPort = -1;
    //private Thread mRecThread;

    public ChatConnection(Handler handler) {
        mUpdateHandler = handler;
        mChatServer = new ChatServer(handler);
    }

//    public void startChatServer(){
//        if(mChatServer!=null)
//            mChatServer.tearDown();
//        mChatServer = new ChatServer(mUpdateHandler);
//    }

    public void tearDown() {
        if (mChatServer != null)
            mChatServer.tearDown();
        if (mChatClient != null)
            mChatClient.tearDown();

    }

    public void connectToServer(InetAddress address, int port) {
        if (mChatClient != null)
            mChatClient.tearDown();
        mChatClient = new ChatClient(address, port);
    }

    public void sendMessage(String msg) {
        if (mChatClient != null) {
            try {
                mChatClient.getmMessageQueue().put(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else
            Log.e(TAG, "Chat Client not initialised");
    }
    
    public int getLocalPort() {
        return mPort;
    }
    
    public void setLocalPort(int port) {
        mPort = port;
    }
    

    public synchronized void updateMessages(String msg, boolean local) {
        Log.e(TAG, "Updating message: " + msg);

        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "socket not connected");
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public ChatServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();

            Log.d(TAG, "Closing server socket.");
            try {
                if (mServerSocket != null)
                    mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertised it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        Socket socket = mServerSocket.accept();
                        Log.d(TAG, "ServerSocket accepted client");
                        setSocket(socket);
                        Log.d(TAG, "Connected.");
//                        Toast toast = Toast.makeText(null, "Connected!", Toast.LENGTH_SHORT);
//                        toast.show();


                        connectToServer(socket.getInetAddress(), socket.getPort());

                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;
        PrintWriter out;
        BufferedReader input;

        //private Socket clientSocket;
        private final String CLIENT_TAG = "ChatClient";

        private Thread mSendThread;
        private Thread mRecThread;
        BlockingQueue<String> mMessageQueue;

        public ChatClient(InetAddress address, int port) {

            Log.d(CLIENT_TAG, "Creating chatClient");
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();

            mRecThread = new Thread(new ReceivingThread());
            mRecThread.start();
        }

        public BlockingQueue<String> getmMessageQueue() {
            return mMessageQueue;
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {
                Log.d(TAG, "Receiving thread initialised");

                while (getSocket() == null) ;

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            getSocket().getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            Log.d(TAG, "Read from the stream: " + messageStr + " -- " + getSocket().toString());
                            updateMessages(messageStr, false);
                        }
//                      else {
//                            Log.d(TAG, "The nulls! The nulls!");
//                            break;
//                        }
                    }


                } catch (IOException e) {
                    Log.e(TAG, "error: ", e);
                }

            }
        }

        class SendingThread implements Runnable {


            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }


            @Override
            public void run() {
                Log.d(CLIENT_TAG, "Client-side sending thread initialised");
                try {
                    if (getSocket() == null || getSocket().getPort() != mPort) {
                        Log.d(CLIENT_TAG, "Client-side socket creating: " + mAddress + "/" + mPort);
                        //getSocket().close();
                        setSocket(new Socket(mAddress, PORT));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");

                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                    }

                    out = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(getSocket().getOutputStream())), true);

                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
                }

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }



        public void tearDown() {
            mSendThread.interrupt();
            mRecThread.interrupt();
            Log.d(CLIENT_TAG, "Closing client socket.");
            try {
                input.close();
                out.close();
                if (getSocket() != null)
                    getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing client socket.");
            }
            Log.d(CLIENT_TAG, "Closed client socket.");
        }

        public void sendMessage(String msg) {
            try {
                if (getSocket() == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (out == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                } else {
                    out.println(msg);
                    out.flush();
                    updateMessages(msg, true);
                    Log.d(CLIENT_TAG, "Sent message: " + msg);
                }
//            } catch (UnknownHostException e) {
//                Log.d(CLIENT_TAG, "Unknown Host", e);
//            } catch (IOException e) {
//                Log.d(CLIENT_TAG, "I/O Exception", e);
//            }
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error", e);
            }

        }
    }


}
