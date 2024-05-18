package study1;
import java.util.*;
import java.text.*;
import java.time.*;
import java.io.*;
import java.net.*;

public class TcpIpServer {
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(7777);
			System.out.println("서버가 준비되었습니다.");
			
			socket = serverSocket.accept();
			
			Sender sender = new Sender(socket);
			Receiver receiver = new Receiver(socket);
			
			sender.start();
			receiver.start();
		} catch (Exception e) {e.printStackTrace();}
	}
}

class Sender extends Thread {
	Socket socket;
	DataOutputStream out;
	String name;
	
	Sender(Socket socket) {
		this.socket = socket;
		try {
			out = new DataOutputStream(socket.getOutputStream());
			name = "[" + socket.getInetAddress() + ":" + socket.getPort() + "]";
		} catch (Exception e) {}
	}
	
	public void run() {
		Scanner scanner = new Scanner(System.in);
		while(out != null) {
			try {
				out.writeUTF(name + scanner.nextLine());
			} catch(IOException e) {}
		}
	}
}

class Receiver extends Thread {
	Socket socket;
	DataInputStream in;
	String name;
	
	Receiver(Socket socket) {
		this.socket = socket;
		try {
			in = new DataInputStream(socket.getInputStream());
			name = "[" + socket.getInetAddress() + ":" + socket.getPort() + "]";
		} catch (Exception e) {}
	}
	
	public void run() {
		Scanner scanner = new Scanner(System.in);
		while(in != null) {
			try {
				System.out.println(in.readUTF());
			} catch(IOException e) {}
		}
	}
}
