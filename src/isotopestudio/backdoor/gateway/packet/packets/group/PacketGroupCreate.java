package isotopestudio.backdoor.gateway.packet.packets.group;

import java.io.IOException;

import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketGroupCreate extends Packet {

	public PacketGroupCreate() {
		super(GROUP_CREATE);
	}

	@Override
	public Packet clone() {
		return new PacketGroupCreate();
	}

	@Override
	public void read() {
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		if(!client.hasGroup()) {
			new Group(client, true);
		} else {
			sendError(client, "lang:group_you_are_already_in_a_group_dialog_title", "lang:group_you_are_already_in_a_group_dialog_message");
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
