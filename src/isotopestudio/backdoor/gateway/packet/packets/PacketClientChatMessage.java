package isotopestudio.backdoor.gateway.packet.packets;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import doryanbessiere.isotopestudio.api.authentification.User;
import doryanbessiere.isotopestudio.api.profile.Profile;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketClientChatMessage extends Packet {

	private Profile profile;
	private long time ;
	private String message;
	
	public PacketClientChatMessage(Profile profile, long time, String message) {
		super(CLIENT_CHAT_MESSAGE, profile.toJson(), time, message);
		
		this.profile = profile;
		this.time = time;
		this.message = message;
	}

	/**
	 * @param id
	 * @param datas
	 */
	public PacketClientChatMessage() {
		super(CLIENT_CHAT_MESSAGE);
	}

	@Override
	public Packet clone() {
		return new PacketClientChatMessage();
	}
	
	@Override
	public void read() {
		message = readString(); 
	}
	
	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * @return the reason
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @return the profile
	 */
	public Profile getProfile() {
		return profile;
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		User user = client.getUser();
		try {
			PacketClientChatMessage packet = new PacketClientChatMessage((profile = new Profile(user.getUUIDString(), user.getUsername(), true, null)), System.currentTimeMillis(), message);
			server.sendPacket(packet, server.clients.toArray(new GatewayRemoteClient[0]));
			Gateway.getMessages().add(packet);
			System.out.println("[CHAT] ["+profile.getUsername()+"] > "+message);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
