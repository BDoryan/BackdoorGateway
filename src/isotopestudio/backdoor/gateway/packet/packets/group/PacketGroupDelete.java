package isotopestudio.backdoor.gateway.packet.packets.group;

import java.io.IOException;
import java.util.ArrayList;

import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketGroupDelete extends Packet {

	public PacketGroupDelete() {
		super(GROUP_DELETE);
	}

	@Override
	public Packet clone() {
		return new PacketGroupDelete();
	}

	@Override
	public void read() {
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		if (client.hasGroup()) {
			if(client.getGroup().getOwner() != client) {
				return;
			}
			client.getGroup().destroy();
		} else {
			sendError(client, "lang:group_you_are_not_in_any_group", "group_you_are_not_in_any_group");
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
