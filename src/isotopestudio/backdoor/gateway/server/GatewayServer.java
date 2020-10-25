package isotopestudio.backdoor.gateway.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.packet.Packet;

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

	public ArrayList<GatewayRemoteClient> clients = new ArrayList<>();

	public GatewayRemoteClient getClient(String username) {
		for (GatewayRemoteClient client : clients) {
			if (client.getUser().getUsername().equals(username)) {
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
			System.out.println("The server is started on port: " + port);
			System.out.println("Waiting clients...");
			while (!this.serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				GatewayRemoteClient client = new GatewayRemoteClient();
				try {
					client.connect(socket);
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							if (client.isConnected() && !client.isAuthenticated()) {
								client.kick("slow_authentication");
								System.err.println("The client(" + socket.getInetAddress().getHostAddress() + ":"
										+ socket.getPort() + ") took too long to connect.");
							}
						}
					}, 5000);
				} catch (SQLException e) {
					e.printStackTrace();
					System.err.println("The client(" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()
							+ ") could not log in, an error occurred!");
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

	/**
	 * @throws IOException
	 * 
	 * Send packet to clients target
	 * 
	 */
	public void sendPacket(Packet packet, GatewayRemoteClient... clients) throws IOException {
		for (GatewayRemoteClient remoteClient : clients) {
			remoteClient.sendPacket(packet);
			Gateway.getLogger().debug("sendPacket("+(remoteClient.getUser() != null ? remoteClient.getUser().getUsername() : remoteClient.getSocket().getInetAddress().getAddress())+", "+Gateway.getGson().toJson(packet)+")");
		}
	}

	/**
	 * @throws IOException
	 *
	 * Send packets to clients target
	 * 
	 */
	public void sendPackets(Packet[] packets, GatewayRemoteClient... clients) throws IOException {
		for (Packet packet : packets) {
			Gateway.getLogger().debug("sendPackets("+Gateway.getGson().toJson(packet)+")");
			sendPacket(packet, clients);
		}
	}
}
