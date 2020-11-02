package isotopestudio.backdoor.gateway.command.commands;

import java.io.IOException;

import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.command.ICommand;
import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class GroupCommand extends ICommand {

	@Override
	public void handle(GatewayRemoteClient remoteclient, String[] args) {
		if (args.length == 0) {
			try {
				remoteclient.sendChatMessage("");
				remoteclient.sendChatMessage("/" + getCommand());
				remoteclient.sendChatMessage(" > " + getDescription());
				remoteclient.sendChatMessage("");
				remoteclient.sendChatMessage("Commands:");
				remoteclient.sendChatMessage(" - /" + getCommand() + " create  | If you create a lobby");
				remoteclient.sendChatMessage(" - /" + getCommand() + " destroy | If you want remove your lobby");
				remoteclient.sendChatMessage(" - /" + getCommand() + " leave | If you want leave the lobby");
				remoteclient.sendChatMessage(" - /" + getCommand() + " list | If you want to see the list of players in your lobby");
				remoteclient.sendChatMessage(" - /" + getCommand()
						+ " invite <username> | If you want invite a other player (online player only)");
				remoteclient.sendChatMessage(" - /" + getCommand()
						+ " accept <username> | If you want to accept an invitation from a player");
				remoteclient.sendChatMessage(" - /" + getCommand()
						+ " uninvite <username> | If you want to remove an invitation from a player");
				remoteclient.sendChatMessage(
						" - /" + getCommand() + " kick <username> | If you want kick a player from your lobby");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args.length == 1) {
			if (args[0].equals("create")) {
				if(!remoteclient.hasGroup()) {
					new Group(remoteclient, true);
					try {
						remoteclient.sendChatMessage("[SUCCESS] Your group has been created!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						remoteclient.sendChatMessage("[ERROR] You are already in a group!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (args[0].equals("list")) {
				if(remoteclient.hasGroup()) {
					Group group = remoteclient.getGroup();
					try {
						remoteclient.sendChatMessage("");
						remoteclient.sendChatMessage("List of players in your group");
						remoteclient.sendChatMessage("'*' = leader of the game");
						remoteclient.sendChatMessage("");
						remoteclient.sendChatMessage("Players ("+group.getPlayers().size()+"):");
						for(GatewayRemoteClient player : group.getPlayers()) {
							remoteclient.sendChatMessage("  - "+(group.getOwner() == player ? "*" : "")+player.getUser().getUsername());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}	
				} else {
					try {
						remoteclient.sendChatMessage("[ERROR] You do not have a group!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (args[0].equals("leave")) {
				if(remoteclient.hasGroup()) {
					if(remoteclient.getGroup().getOwner() == remoteclient) {
						try {
							remoteclient.sendChatMessage("[ERROR] You cannot leave your own group!");
						} catch (IOException e) {
							e.printStackTrace();
						}
						return;
					}
					try {
						remoteclient.sendChatMessage("[SUCCESS] You have left the group!");
						
						Group group = remoteclient.getGroup();
						group.leave(remoteclient);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						remoteclient.sendChatMessage("[ERROR] You do not have a group!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (args[0].equals("destroy")) {
				if(remoteclient.hasGroup()) {
					if(remoteclient.getGroup().getOwner() != remoteclient) {
						try {
							remoteclient.sendChatMessage("[ERROR] You are not the leader of the group!");
						} catch (IOException e) {
							e.printStackTrace();
						}
						return;
					}
					try {
						remoteclient.sendChatMessage("[SUCCESS] You have destroy your group!");
						
						remoteclient.getGroup().destroy();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						remoteclient.sendChatMessage("[ERROR] You do not have a group!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else if (args.length == 2) {
			String targetUsername = args[1];

			GatewayRemoteClient targetClient = Gateway.getGatewayServer().getClient(targetUsername);
			if (targetClient == null) {
				try {
					remoteclient.sendChatMessage("[ERROR] This player is not online!");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			
			if (args[0].equals("accept")) {
				if (!targetClient.hasGroup()) {
					try {
						remoteclient.sendChatMessage("[ERROR] This player does not have a group!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				Group group = targetClient.getGroup();

				if (group.join(remoteclient)) {
					try {
						remoteclient.sendChatMessage("[SUCCESS] You have joined "+group.getOwner().getUser().getUsername()+"'s group!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						remoteclient.sendChatMessage("[ERROR] You do not have access to this group!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				return;
			}

			if (!remoteclient.hasGroup()) {
				try {
					remoteclient.sendChatMessage("[ERROR] You do not have a group!");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			if (remoteclient.getGroup().getOwner() != remoteclient) {
				try {
					remoteclient.sendChatMessage("[ERROR] You are not the head of the group!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (args[0].equals("invite")) {
				if(targetClient == remoteclient) {
					try {
						remoteclient.sendChatMessage("[ERROR] You cannot invite yourself!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}

				Group group = remoteclient.getGroup();
				targetClient.invite(group);
				try {
					remoteclient.sendChatMessage("[SUCCESS] "+targetClient.getUser().getUsername()+"'s invitation has been sent!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (args[0].equals("uninvite")) {
				Group group = remoteclient.getGroup();
				group.unwhitelist(targetClient.getUser().getUUIDString());
				try {
					remoteclient.sendChatMessage("[SUCCESS] "+targetClient.getUser().getUsername()+"'s invitation to be removed!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (args[0].equals("kick")) {
				if(targetClient == remoteclient) {
					try {
						remoteclient.sendChatMessage("[ERROR] You can't kicked yourself!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}

				Group group = remoteclient.getGroup();
				if (!group.getPlayers().contains(targetClient)) {
					try {
						remoteclient.sendChatMessage("[ERROR] This player is not in your group!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				} 
				
				if(group.kick(targetClient)) {
					try {
						remoteclient.sendChatMessage("[SUCCESS] "+targetClient.getUser().getUsername()+" has been successfully kicked!");
						targetClient.sendChatMessage("You have been kicked from the group");
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						remoteclient.sendChatMessage("[ERROR] This player could not be ejected!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			try {
				remoteclient.sendChatMessage("[ERROR] Invalid command: /"+getCommand());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getCommand() {
		return "group";
	}

	@Override
	public String getDescription() {
		return "Allows you to manage your group";
	}
}
