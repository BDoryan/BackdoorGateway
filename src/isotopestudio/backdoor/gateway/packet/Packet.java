package isotopestudio.backdoor.gateway.packet;

import isotopestudio.backdoor.gateway.packet.packets.PacketClientChatMessage;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientChatMessages;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientConnectToServer;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientDisconnected;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientReceiveNotification;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupAccept;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupCreate;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupDelete;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupInvite;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupKick;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupLeave;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupReady;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupSet;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupStart;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupUpdate;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

public abstract class Packet {

	public static final char SEPARATOR = '/';

	public static final int CLIENT_DISCONNECTED = 1;
	public static final int CLIENT_CHAT_MESSAGE = 2;
	public static final int CLIENT_CHAT_MESSAGES = 3;
	public static final int CLIENT_RECEIVE_NOTIFICATION = 4;
	public static final int CLIENT_CONNECT_TO_SERVER = 5;
	public static final int GROUP_UPDATE = 6;
	public static final int GROUP_CREATE = 7;
	public static final int GROUP_DELETE = 8;
	public static final int GROUP_LEAVE = 9;
	public static final int GROUP_SET = 10;
	public static final int GROUP_INVITE = 11;
	public static final int GROUP_KICK = 12;
	public static final int GROUP_ACCEPT = 13;
	public static final int GROUP_READY = 14;
	public static final int GROUP_START = 15;

	public static Packet[] packets = new Packet[] { null,
		new PacketClientDisconnected(),
		new PacketClientChatMessage(),
		new PacketClientChatMessages(),
		new PacketClientReceiveNotification(),
		new PacketClientConnectToServer(),
		new PacketGroupUpdate(),
		new PacketGroupCreate(),
		new PacketGroupDelete(),
		new PacketGroupLeave(),
		new PacketGroupSet(),
		new PacketGroupInvite(),
		new PacketGroupKick(),
		new PacketGroupAccept(),
		new PacketGroupReady(),
		new PacketGroupStart()
	};

	public static Packet parsePacket(String data) {
		String[] datas = data.split(SEPARATOR + "");
		int packet_id = Integer.parseInt(datas[0].trim());
		if (packet_id == 0) {
			System.err.println("Invalid packet");
			return null;
		}

		if (packet_id > packets.length) {
			System.err.println("Invalid packet (packet_id invalid)");
			return null;
		}

		Packet Packet = packets[packet_id].clone();
		Packet.id = packet_id;
		Packet.datas = datas;
		return Packet;
	}

	private int id;
	public String[] datas;

	public Packet(int id, Object... datas) {
		super();
		this.id = id;
		this.datas = new String[datas.length + 1];
		this.datas[0] = id + "";
		for (int i = 1; i < datas.length + 1; i++) {
			String data = datas[i - 1] != null ? datas[i - 1] + "" : "null";
			this.datas[i] = data.replace(SEPARATOR+"", "%20");
		}
	}

	public String toData() {
		String data = this.id + "";
		if (this.datas.length > 1) {
			for (int i = 1; i < this.datas.length; i++) {
				data += SEPARATOR + this.datas[i];
			}
		}
		return data;
	}

	public int getId() {
		return id;
	}

	public abstract Packet clone();

	public abstract void read();

	public abstract void process(GatewayServer server, GatewayRemoteClient client);

	// for skip the first information (packet id)
	int read_index = 1;

	public String readString() throws IllegalStateException {
		String data = datas[read_index];
		read_index++;
		String data_string = data.trim().replace("%20", SEPARATOR+"");
		if(data_string.equalsIgnoreCase("null"))
			return null;
		return data_string;
	}

	public Boolean readBoolean() {
		return Boolean.parseBoolean(readString());
	}

	public Integer readInt() {
		return Integer.parseInt(readString());
	}

	public Long readLong() {
		return Long.parseLong(readString());
	}

	public Float readFloat() {
		return Float.parseFloat(readString());
	}

	public Double readDouble() {
		return Double.parseDouble(readString());
	}

	public static Packet get(int id) {
		return packets[id];
	}
}
