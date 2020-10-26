package isotopestudio.backdoor.gateway.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import doryanbessiere.isotopestudio.api.authentification.User;
import doryanbessiere.isotopestudio.commons.mysql.SQLDatabase;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientDisconnected;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class GatewayRemoteClient extends Thread {

	private User user;
	private Socket socket;

	private DataOutputStream output;
	private DataInputStream input;

	private boolean connected;
	private boolean authenticated;

	public void connect(Socket socket) throws IOException, SQLException {
		this.socket = socket;
		this.output = new DataOutputStream(this.socket.getOutputStream());
		this.input = new DataInputStream(this.socket.getInputStream());
		connected = true;
		start();
	}

	@Override
	public void run() {
		SQLDatabase database = Gateway.getDatabase();

		try {
			String[] authentification = input.readUTF().split(";");
			if(authentification.length == 3) {
				if(!authentification[0].equals(Gateway.getVersion())) {
					kick("wrong_version");	
					return;
				}
				if (database.has("users", "email", authentification[1])) {
					if (database.getString("users", "email", authentification[1], "token").equals(authentification[2])) {
						authenticated = true;
						this.user = User.fromJson(database.getString("users_data", "uuid",
								database.getString("users", "email", authentification[1], "uuid"), "json"));
						Gateway.getGatewayServer().clients.add(this);
						connected();
						return;
					}
				}
				kick("authentification_failed");	
			} else {
				kick("authentification_invalid");	
			}
		} catch (IOException | SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	private void connected() {
		System.out.println("[" + (user != null ? user.getUsername()
				: socket.getInetAddress().getHostAddress()) + "] is logged into the gateway :D");
		while (connected && authenticated) {
			try {
				Packet packet = readPacket();
				try {
					packet.process(Gateway.getGatewayServer(), this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				connected = false;
			}
		}
		disconnected();
	}

	public void kick(String reason) {
		try {
			sendPacket(new PacketClientDisconnected(PacketClientDisconnected.KICKED, reason));
			close();
			connected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnected() {
		System.out.println("[" + (user != null ? user.getUsername()
				: socket.getInetAddress().getHostAddress()) + "] is no longer connected to the gateway.");
		Gateway.getGatewayServer().clients.remove(this);
		try {
			socket.close();
		} catch (Exception e) {
		}
		setConnected(false);
	}

	public void sendData(String data) throws IOException {
		write(data);
	}

	public void sendPacket(Packet packet) throws IOException {
		sendData(packet.toData());
	}

	public Packet readPacket() throws IOException {
		Packet packet = Packet.parsePacket(read());
		packet.read();
		// System.out.println("[Packet] receive -> "+packet.toData());
		return packet;
	}

	public void write(String data) throws IOException {
		// output.writeInt(bytes.length);
		output.writeUTF(data);
		output.flush();
	}

	public void close() {
		try {
			if(!socket.isClosed())
			socket.close();
			input.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String read() throws IOException {
		return input.readUTF();
	}

	/**
	 * @return the connection status
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @return the authenticated
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * @param b
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
