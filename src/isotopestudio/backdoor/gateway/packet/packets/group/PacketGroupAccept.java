package isotopestudio.backdoor.gateway.packet.packets.group;

import java.io.IOException;
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
public class PacketGroupAccept extends Packet {

	public PacketGroupAccept() {
		super(GROUP_ACCEPT);
	}

	public PacketGroupAccept(String playerUUID) {
		super(GROUP_ACCEPT, playerUUID);
		this.playerUUID = playerUUID;
	}

	@Override
	public Packet clone() {
		return new PacketGroupAccept();
	}

	private String playerUUID;

	@Override
	public void read() {
		this.playerUUID = readString();
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		GatewayRemoteClient targetClient = Gateway.getGatewayServer().getClient(UUID.fromString(playerUUID));
		if (targetClient == null) {
			try {
				client.sendNotification("error", "lang:group_player_target_disconnected_title",
						"lang:group_player_target_disconnected_message");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		if (!targetClient.hasGroup()) {
			try {
				targetClient.sendNotification("error", "lang:group_you_are_not_in_any_group", "lang:group_you_are_not_in_any_group");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		Group group = targetClient.getGroup();
		
		if(group.getQueue() != null) {
			try {
				targetClient.sendNotification("error", "lang:impossible_to_join_the_group", "lang:this_group_is_already_in_a_queue");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		if (group.getPlayers().contains(client)) {
			return;
		}

		if (client.hasGroup()) {
			client.getGroup().leave(client);
		}

		if(group.isFull()) {
			try {
				targetClient.sendNotification("error", "lang:group_the_group_is_complete_title", "lang:group_the_group_is_complete_message");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		if (group.getOwner() == targetClient) {
			group.join(client);
		}
	}
}
