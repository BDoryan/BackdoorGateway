package isotopestudio.backdoor.gateway.packet.packets;

import java.io.IOException;

import doryanbessiere.isotopestudio.api.profile.Profile;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BDoryan
 * @github https://www.github.com/BDoryan/
 */
public class PacketClientChatMessages extends Packet {

	public PacketClientChatMessages(Profile[] profiles, long[] times, String[] messages, int count) {
		super(CLIENT_CHAT_MESSAGES, Gateway.getGson().toJson(profiles), Gateway.getGson().toJson(times), Gateway.getGson().toJson(messages), count);
	}
	
	public PacketClientChatMessages() {
		super(CLIENT_CHAT_MESSAGES);
	}

	@Override
	public Packet clone() {
		return new PacketClientChatMessages();
	}
	
	@Override
	public void read() {
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		int count = Gateway.getMessages().size();
		if(count == 0)return;
		Profile[] profiles = new Profile[count];
		long[] times = new long[count];
		String[] messages = new String[count];
		
		int index = 0;
		
		for(PacketClientChatMessage packet : Gateway.getMessages()) {
			profiles[index] = packet.getProfile();
			times[index] = packet.getTime()	;
			messages[index] = packet.getMessage();
			index++;
		}
		
		try {
			server.sendPacket(new PacketClientChatMessages(profiles, times, messages, count), client);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
