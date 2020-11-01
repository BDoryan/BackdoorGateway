package isotopestudio.backdoor.gateway.command.commands;

import java.io.IOException;

import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.command.ICommand;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientReceiveNotification;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class NotificationCommand extends ICommand {

	@Override
	public void handle(GatewayRemoteClient remoteClient, String[] args) {
		if(remoteClient.getUser().getPermissionLevel() < 1)return;

		int startOff = 1;
		
		if(args[0].startsWith("@")) {
			String playerName = args[0].substring(1);
			
			GatewayRemoteClient targetClient = null;
			
			if((targetClient = Gateway.getGatewayServer().getClient(playerName)) == null) {
				try {
					remoteClient.sendChatMessage("[ERROR] This player is not online!");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}


			startOff = 2;
			String image_path = args[1];
			String title = calcString(startOff, args);
			String message = calcString(title.split(" ").length + startOff, args);;
			
			try {
				targetClient.sendNotification(image_path, title, message);
				remoteClient.sendChatMessage("[SUCCESS] The notification was successfully sent to "+targetClient.getUser().getUsername()+".");
			} catch (IOException e) {
				e.printStackTrace();
				try {
					remoteClient.sendChatMessage("[ERROR] The notification could not be sent due to a problem... (contact the developers) !");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			String image_path = args[0];
			String title = calcString(startOff, args);
			String message = calcString(title.split(" ").length + startOff, args);
			try {
				Gateway.getGatewayServer().sendPacketToClients(new PacketClientReceiveNotification(image_path, title, message));
				remoteClient.sendChatMessage("[SUCCESS] Notifications have been sent to the players.");
			} catch (IOException e) {
				e.printStackTrace();
				try {
					remoteClient.sendChatMessage("[ERROR] The notification could not be sent due to a problem... (contact the developers) !");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private String calcString(int index, String[] args) {
		String string = "";
		if(args[index].startsWith("\"")) {
			if(args[index].endsWith("\"")) {
				string = args[index].substring(1, args[index].length() - 2);	
			} else {
				string = args[index].substring(1);
				while(true) {
					index++;
					if(args[index].endsWith("\"")) {
						string += " "+args[index].substring(0, args[index].length() - 1);
						break;
					} else {
						string += " "+args[index];
					}
				}
			}
		} 
		return string;
	}
	
	@Override
	public String getCommand() {
		return "notification";
	}

	@Override
	public String getDescription() {
		return "Allows you to send notification to client or clients";
	}
}
