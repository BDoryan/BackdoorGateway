package isotopestudio.backdoor.gateway.packet.packets.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BDoryan
 * @github https://www.github.com/BDoryan/
 */
public class PacketGroupKick extends Packet {

	public PacketGroupKick() {
		super(GROUP_KICK);
	}

	public PacketGroupKick(String playerUUID) {
		super(GROUP_KICK, playerUUID);
		this.playerUUID = playerUUID;
	}

	@Override
	public Packet clone() {
		return new PacketGroupKick();
	}

	private String playerUUID;
	
	@Override
	public void read() {
		this.playerUUID = readString();
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		if(client.hasGroup()) {
			Group group = client.getGroup();
			if(group.getOwner() == client) {
				GatewayRemoteClient targetClient = Gateway.getGatewayServer().getClient(UUID.fromString(playerUUID));
				if(targetClient != null && targetClient.hasGroup() && targetClient.getGroup() == group) {
					group.kick(targetClient);
				}
			}
		} else {
			try {
				client.sendNotification("error",  "lang:group_you_are_not_in_any_group_title", "lang:group_you_are_not_in_any_group_message");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
