package gateway.isotopestudio.fr.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class GatewayServer extends Thread {
	
	private int port;

	public GatewayServer(int port) {
		super();
		this.port = port;
	}
	
	public ArrayList<GatewayClient> clients = new ArrayList<>();
	
	public GatewayClient getClient(String username) {
		for(GatewayClient client : clients) {
			if(client.getUser().getUsername().equals(username)) {
				return client;
			}
		}
		return null;
	}
	
	private ServerSocket serverSocket;
	
	@Override
	public void run() {
		try {
			this.serverSocket = new ServerSocket(port);
			
			while(!this.serverSocket.isClosed()) {
				Socket socket = new Socket();
				GatewayClient client = new GatewayClient();
				try {
					client.connect(socket);
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							if(client.isConnected() && !client.isAuthenticated()) {
								client.kick("slow_authentication");
								System.err.println("The client("+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+") took too long to connect.");
							}
						}
					}, 5000);
				} catch (SQLException e) {
					e.printStackTrace();
					System.err.println("The client(\"+socket.getInetAddress().getHostAddress()+\":\"+socket.getPort()+\") could not log in, an error occurred!");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Gateway Server cannot start!");
			System.exit(IsotopeStudioAPI.EXIT_CODE_CRASH);
		}
	}
	
	/**
	 * Allows you to close tcp server and kill the thread
	 */
	public void close() {
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the serverSocket
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
}
