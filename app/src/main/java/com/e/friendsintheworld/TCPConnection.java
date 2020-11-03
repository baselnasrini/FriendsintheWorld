package com.e.friendsintheworld;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.e.friendsintheworld.controllers.MainController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public class TCPConnection extends Service {
	public static final String IP="IP",PORT="PORT"; //
	private RunOnThread thread;
	private Receive receive;
	private Buffer<String> receiveBuffer; //
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private InetAddress address;
	private int connectionPort;
	private String ip;
	private Exception exception;
	private SharedViewModel sharedViewModel;


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.ip = intent.getStringExtra(IP);
		this.connectionPort = Integer.parseInt(intent.getStringExtra(PORT));
		thread = new RunOnThread();
		receiveBuffer = new Buffer<String>();
		//sharedViewModel = ViewModelProviders.of(intent.getA.get(SharedViewModel.class);
		Log.v("TCPConnection", "new TCPConnection");
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new LocalService();
	}

	public void connect() {
		Log.v("TCPConnection connect()", "TCPConnection connect()");

		thread.start();
		thread.execute(new Connect());
	}

	public void disconnect() {
		thread.execute(new Disconnect());
	}

	public void send(String type, String[] values) {
		String msgStr = MessageHandler.JSONMessage(type,values);
		Log.v("sent message" , msgStr);
		thread.execute(new Send(msgStr));
	}
	
	public String receive() throws InterruptedException {
		Log.v("recieved message" ,"recieved msg");

		return receiveBuffer.get();
	}

    public class LocalService extends Binder {
        public TCPConnection getService() {
            return TCPConnection.this;
        }
    }

	private class Receive extends Thread {
		public void run() {
			String result;
			try {
				Log.v("Receive try", "Receive try");
				while (receive != null) {
					Log.v("run: receiving...", "run: receiving...");
					result = input.readUTF();
					//(MainController.mainController).messageReceived(result);
					receiveBuffer.put(result);
				}
			} catch (Exception e) { // IOException, ClassNotFoundException
				Log.v("Receive run exception", e.toString());
				receive = null;
			}
		}
	}

	private class Connect implements Runnable {
		public void run() {
			try {
				address = InetAddress.getByName(ip);
				socket = new Socket(address, connectionPort);
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				output.flush();
				receive = new Receive();
				receive.start();
				Log.v("Connect constructor", "CONNECTED!!!!");

			} catch (Exception e) { // SocketException, UnknownHostException
				exception = e;
				Log.v("Connect exception: ", "exception: " + e);
				receiveBuffer.put("EXCEPTION");
			}
		}
	}

	private class Disconnect implements Runnable {
		public void run() {
			try {
				if (input != null)
				    input.close();
			    if (output != null)
				    output.close();
			    if (socket != null)
				    socket.close();
			    thread.stop();
			    receive = null;
				Log.v("Disconnect constructor", "DISCONNECTED!!!!");

			} catch(IOException e) {
				exception = e;
				Log.v("Disconnect exception: ", "exception: " + e);
				receiveBuffer.put("EXCEPTION");
			}
		}
	}

	private class Send implements Runnable {
		private String message;

		public Send(String msg) {
			this.message = msg;
		}

		public void run() {
			try {
				output.writeUTF(message);
				output.flush();
			} catch (IOException e) {
				exception = e;
				Log.v("Send exception: ", "exception: " + e);
				receiveBuffer.put("EXCEPTION");
			}
		}
	}
}
