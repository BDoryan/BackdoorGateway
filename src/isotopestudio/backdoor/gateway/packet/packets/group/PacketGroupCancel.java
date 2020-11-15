package isotopestudio.backdoor.gateway.packet.packets.group;

import java.io.IOException;

import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BDoryan
 * @github https://www.github.com/BDoryan/
 */
public class PacketGroupCancel extends Packet {

	public PacketGroupCancel() {
		super(GROUP_CANCEL);
	}

	@Override
	public Packet clone() {
		return new PacketGroupCancel();
	}

	@Override
	public void read() {
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		if (client.hasGroup()) {
			Group group = client.getGroup();
			if(group.getQueue() != null) {
				group.getQueue().leave(group);
				group.message(PacketGroupMessage.WARNING, "lang:the_group_to_leave_the_queue", 5000);
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
