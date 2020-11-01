package isotopestudio.backdoor.gateway.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientReceiveNotification;

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
			if (client.getUser().getUsername().equalsIgnoreCase(username)) {
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

	public GatewayRemoteClient getClient(UUID uuid) {
		for(GatewayRemoteClient remoteClient : getClients()){
			if(remoteClient.getUser().getUUIDString().equals(uuid.toString())) {
				return remoteClient;
			}
		}
		return null;
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
	 * @return the clients
	 */
	public ArrayList<GatewayRemoteClient> getClients() {
		return clients;
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

	/**
	 * @param packet
	 * @throws IOException 
	 */
	public void sendPacketToClients(Packet packet) throws IOException {
		sendPacket(packet, getClients().toArray(new GatewayRemoteClient[1]));
	}

	/**
	 * @param packet
	 * @throws IOException 
	 */
	public void sendPacketsToClients(Packet[] packets) throws IOException {
		sendPackets(packets, getClients().toArray(new GatewayRemoteClient[1]));
	}
}
