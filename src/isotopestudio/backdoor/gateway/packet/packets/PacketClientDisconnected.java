package isotopestudio.backdoor.gateway.packet.packets;

import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketClientDisconnected extends Packet {

	public static final int DISCONNECTED = 0;
	public static final int KICKED = 1;

	private int type;
	private String reason;
	
	public PacketClientDisconnected(int type, String reason) {
		super(CLIENT_DISCONNECTED, type, reason);
	}

	/**
	 * @param id
	 * @param datas
	 */
	public PacketClientDisconnected() {
		super(CLIENT_DISCONNECTED);
	}

	@Override
	public Packet clone() {
		return new PacketClientDisconnected();
	}
	
	@Override
	public void read() {
		type = readInt();
		reason = readString(); 
	}
	
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		if (type == DISCONNECTED)
			System.out.println("["+(client.getUser() != null ? client.getUser().getUsername() : client.getSocket().getInetAddress().getHostAddress()) +"] have been disconnected from the gateway.");
		else
			System.out.println("["+(client.getUser() != null ? client.getUser().getUsername() : client.getSocket().getInetAddress().getHostAddress()) +"] have been kicked from the gateway. (reason="+getReason()+")");
		
		client.setConnected(false);
	}
}
