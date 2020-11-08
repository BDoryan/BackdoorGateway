package isotopestudio.backdoor.gateway.packet.packets.group;

import doryanbessiere.isotopestudio.commons.GsonInstance;
import isotopestudio.backdoor.gateway.group.GroupObject;
import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketGroupUpdate extends Packet {

	private GroupObject group;

	public PacketGroupUpdate() {
		super(GROUP_UPDATE);
	}

	public PacketGroupUpdate(GroupObject group) {
		super(GROUP_UPDATE, group == null ? null : GsonInstance.instance().toJson(group));
		this.group = group;
	}

	@Override
	public Packet clone() {
		return new PacketGroupUpdate();
	}

	@Override
	public void read() {
		String groupJson = readString();
		if (groupJson == null) {
			this.group = null;
			return;
		}
		this.group = GsonInstance.instance().fromJson(groupJson, GroupObject.class);
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
	}
	
	/**
	 * @return the group
	 */
	public GroupObject getGroup() {
		return this.group;
	}
}
