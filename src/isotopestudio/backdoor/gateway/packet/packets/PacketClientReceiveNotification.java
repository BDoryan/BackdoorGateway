package isotopestudio.backdoor.gateway.packet.packets;

import isotopestudio.backdoor.gateway.packet.Packet;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PacketClientReceiveNotification extends Packet {

	private String image_path;
	private String title;
	private String message;
	private int duration;

	public PacketClientReceiveNotification(String image_path, String title, String message, int duration) {
		super(CLIENT_RECEIVE_NOTIFICATION, image_path, title, message, duration);
		
		this.image_path = image_path;
		this.title = title;
		this.message = message;
		this.duration = duration;
	}

	public PacketClientReceiveNotification(String image_path, String title, String message) {
		super(CLIENT_RECEIVE_NOTIFICATION, image_path, title, message, 10);
		
		this.image_path = image_path;
		this.title = title;
		this.message = message;
		this.duration = 10;
	}
	
	/**
	 * @return the image_path
	 */
	public String getImagePath() {
		return image_path;
	}
	
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @param id
	 * @param datas
	 */
	public PacketClientReceiveNotification() {
		super(CLIENT_RECEIVE_NOTIFICATION);
	}

	@Override
	public Packet clone() {
		return new PacketClientReceiveNotification();
	}
	
	@Override
	public void read() {
	}

	@Override
	public void process(GatewayServer server, GatewayRemoteClient client) {
	}
}
