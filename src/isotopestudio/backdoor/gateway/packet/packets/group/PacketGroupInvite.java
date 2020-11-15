package isotopestudio.backdoor.gateway.packet.packets.group;

import java.io.IOException;
import java.util.UUID;

import doryanbessiere.isotopestudio.api.profile.Profile;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BDoryan
 * @github https://www.github.com/BDoryan/
 */
public class PacketGroupInvite extends Packet {

	public PacketGroupInvite() {
		super(GROUP_INVITE);
	}
	
	public PacketGroupInvite(Profile player) {
		super(GROUP_INVITE, player.toJson());
		this.player = player;
	}

	@Override
	public Packet clone() {
		return new PacketGroupInvite();
	}
	
	private Profile player;

	@Override
	public void read() {
		this.player = Profile.fromJson(readString());
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		if(client.hasGroup()) {
			Group group = client.getGroup();
			if(group.getOwner() == client) {
				GatewayRemoteClient targetClient = Gateway.getGatewayServer().getClient(UUID.fromString(player.getUuidString()));
				if(targetClient != null) {
					if(client == targetClient)return;

					if(group.getPlayers().contains(targetClient))return;
					if(group.getWhitelist().contains(targetClient.getUser().getUUIDString()))return;


					if(client.getGroup().isFull()) {
						try {
							targetClient.sendNotification("error", "lang:group_the_group_is_complete_title", "lang:group_the_group_is_complete_message");
						} catch (IOException e) {
							e.printStackTrace();
						}
						return;
					}
					
					group.whitelist(targetClient.getUser().getUUIDString());
					try {
						targetClient.sendPacket(new PacketGroupInvite(client.getProfile()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			try {
				client.sendNotification("error", "lang:group_you_are_not_in_any_group_title", "lang:group_you_are_not_in_any_group_message");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
