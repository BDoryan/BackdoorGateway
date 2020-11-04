package isotopestudio.backdoor.gateway.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.UUID;

import doryanbessiere.isotopestudio.api.authentification.User;
import doryanbessiere.isotopestudio.api.profile.Profile;
import doryanbessiere.isotopestudio.commons.mysql.SQLDatabase;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientChatMessage;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientDisconnected;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientReceiveNotification;
import isotopestudio.backdoor.gateway.party.Party;

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

	private Group group;
	private Party party;

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
			String[] authentification = read().split(";");
			if (authentification.length == 3) {
				if (!authentification[0].equals(Gateway.getVersion())) {
					kick("wrong_version");
					return;
				}
				if (database.has("users", "email", authentification[1])) {
					if (database.getString("users", "email", authentification[1], "token")
							.equals(authentification[2])) {
						UUID uuidString = UUID.fromString(database.getString("users", "email", authentification[1], "uuid"));
						if(Gateway.getGatewayServer().getClient(uuidString) != null) {
							kick("user_already_connected");
							return;
						}
						authenticated = true;
						this.user = User.fromJson(database.getString("users_data", "uuid",
								uuidString.toString(), "json"));
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

	public void sendChatMessage(String message) throws IOException {
		Profile profile = new Profile(UUID.randomUUID().toString(), "SERVER", true, null);
		sendPacket(new PacketClientChatMessage(profile, System.currentTimeMillis(), message.isEmpty() ? " " : message));
	}

	private void connected() {
		System.out.println("[" + (user != null ? user.getUsername() : socket.getInetAddress().getHostAddress())
				+ "] is logged into the gateway :D");
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
		if (hasGroup()) {
			if (getGroup().getOwner().equals(this)) {
				getGroup().destroy();
			} else {
				getGroup().leave(this);
			}
		}

		System.out.println("[" + (user != null ? user.getUsername() : socket.getInetAddress().getHostAddress())
				+ "] is no longer connected to the gateway.");
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

	/**
	 * @throws IOException
	 *
	 *                     Send packet to clients target
	 * 
	 */
	public void sendPacket(Packet packet) throws IOException {
		String data = packet.toData();
		sendData(data);
		Gateway.getLogger()
				.debug("sendPacket("
						+ (getUser() != null ? getUser().getUsername() : getSocket().getInetAddress().getAddress())
						+ ", " + data + ")");
	}

	/**
	 * @throws IOException
	 *
	 *                     Send packets to server
	 * 
	 */
	public void sendPackets(Packet[] packets) throws IOException {
		for (Packet packet : packets) {
			sendPacket(packet);
		}
	}

	public Packet readPacket() throws IOException {
		String data = read();
		Packet packet = Packet.parsePacket(data);
		packet.read();
		Gateway.getLogger()
				.debug("readPacket("
						+ (getUser() != null ? getUser().getUsername() : getSocket().getInetAddress().getAddress())
						+ ", " + data + ")");
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
			if (!socket.isClosed())
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
	 * @param party the party to set
	 */
	public void setParty(Party party) {
		this.party = party;
	}

	/**
	 * @return the party
	 */
	public Party getParty() {
		return party;
	}

	/**
	 * @return if you are in a party
	 */
	public boolean inParty() {
		return getParty() != null;
	}

	/**
	 * @param Group2 the Group to set
	 */
	public void setGroup(Group group) {
		this.group = group;
	}

	/**
	 * @param Group2
	 */
	public void invite(Group group) {
		group.whitelist(getUser().getUUIDString());
	}

	/**
	 * @return the Group
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * @return true if you are in a Group
	 */
	public boolean hasGroup() {
		return group != null;
	}

	/**
	 * @param b
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	/**
	 * @param image_path
	 * @param title
	 * @param message
	 * 
	 * @throws IOException
	 */
	public void sendNotification(String image_path, String title, String message) throws IOException {
		sendPacket(new PacketClientReceiveNotification(image_path, title, message));
	}
}
