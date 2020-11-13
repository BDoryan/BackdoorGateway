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
			try {
				client.sendNotification("error", "lang:group_you_are_already_in_a_group_dialog_title", "lang:group_you_are_already_in_a_group_dialog_message");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
