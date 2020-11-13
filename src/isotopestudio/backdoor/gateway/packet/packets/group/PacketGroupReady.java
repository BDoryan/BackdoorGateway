package isotopestudio.backdoor.gateway.packet.packets.group;

import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketGroupReady extends Packet {

	public PacketGroupReady() {
		super(GROUP_READY);
	}

	public PacketGroupReady(boolean ready) {
		super(GROUP_READY, ready);
		this.ready = ready;
	}

	@Override
	public Packet clone() {
		return new PacketGroupReady();
	}

	private boolean ready;

	@Override
	public void read() {
		this.ready = readBoolean();
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
		if(client.hasGroup()) {
			if(client.getGroup().getOwner() == client)return;
			if(ready)
				client.getGroup().ready(client.getUser().getUUIDString());
			else 
				client.getGroup().unready(client.getUser().getUUIDString());
		}
	}
}
