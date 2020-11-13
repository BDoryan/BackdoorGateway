package isotopestudio.backdoor.gateway.packet.packets.group;

import java.io.IOException;

import isotopestudio.backdoor.core.gamemode.GameMode;
import isotopestudio.backdoor.core.server.configuration.GameServerConfiguration;
import isotopestudio.backdoor.core.versus.Versus;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.party.Party;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketGroupStart extends Packet {

	public PacketGroupStart() {
		super(GROUP_START);
	}

	@Override
	public Packet clone() {
		return new PacketGroupStart();
	}

	@Override
	public void read() {
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		if (client.hasGroup()) {
			GameMode gameMode = client.getGroup().getGamemode();
			Versus versus = client.getGroup().getVersus();
			Group group = client.getGroup();
			if (group.getOwner() != client) {
				return;
			}
			if(client.getGroup().isOverload()) {
				sendError(client, "lang:group_the_group_is_complete_title", "lang:group_the_group_is_complete_message");
				return;
			}
			if (!group.allPlayersAreReady()) {
				return;
			}
			if (!group.allPlayersAreAvailable()) {
				sendError(client, "lang:group_cant_start_title", "lang:group_a_player_is_already_in_party_message");
				return;
			}
			
			if(group.isPrivate()) {
				GameServerConfiguration configuration = new GameServerConfiguration(Gateway.generatePort(), null, false,
						gameMode, versus, null, null, null, null, true);
				Party party = new Party(configuration);
				try {
					party.build();
				} catch (Exception e) {
					sendError(client, "lang:party_start_error_title",
							"lang:party_start_error_message");
					e.printStackTrace();
					party.unbuild();
					return;
				}
				party.start();
				party.connect(group.getPlayers());	
			} else {
				
			}
		} else {
			sendError(client, "lang:group_you_are_already_in_a_group_dialog_title",
					"lang:group_you_are_already_in_a_group_dialog_message");
		}
	}

	private void sendError(GatewayRemoteClient client, String title, String message) {
		client.getGroup().getPlayers().forEach((remoteplayer) -> {
			try {
				remoteplayer.sendNotification("error", title, message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
