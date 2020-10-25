package isotopestudio.backdoor.gateway.packet.packets;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import doryanbessiere.isotopestudio.api.authentification.User;
import doryanbessiere.isotopestudio.api.profile.Profile;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketClientChat extends Packet {

	private Profile profile;
	private String message;
	
	public PacketClientChat(Profile profile, String message) {
		super(CLIENT_CHAT, profile.toJson(), message);
	}

	/**
	 * @param id
	 * @param datas
	 */
	public PacketClientChat() {
		super(CLIENT_CHAT);
	}

	@Override
	public Packet clone() {
		return new PacketClientChat();
	}
	
	@Override
	public void read() {
		message = readString(); 
	}
	
	/**
	 * @return the reason
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		User user = client.getUser();
		try {
			server.sendPacket(new PacketClientChat((profile = new Profile(user.getUUIDString(), user.getUsername(), true, null)), message), server.clients.toArray(new GatewayRemoteClient[0]));
			System.out.println("[CHAT] ["+profile.getUsername()+"] > "+message);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
