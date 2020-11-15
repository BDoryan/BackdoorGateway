package isotopestudio.backdoor.gateway.packet.packets.group;

import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BDoryan
 * @github https://www.github.com/BDoryan/
 */
public class PacketGroupMessage extends Packet {

	public static final String ERROR = "error";
	public static final String WARNING = "warning";
	public static final String SUCCESS = "success";

	public PacketGroupMessage() {
		super(GROUP_MESSAGE);
	}

	private String type;
	private String message;
	
	public PacketGroupMessage(String type, String message) {
		super(GROUP_MESSAGE, type, message);
		
		this.type = type;
		this.message = message;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
	@Override
	public Packet clone() {
		return new PacketGroupMessage();
	}

	@Override
	public void read() {
		this.type = readString();
		this.message = readString();
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
	}
}
