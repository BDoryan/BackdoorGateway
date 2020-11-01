package isotopestudio.backdoor.gateway.command.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;

import isotopestudio.backdoor.core.gamemode.GameMode;
import isotopestudio.backdoor.core.server.configuration.GameServerConfiguration;
import isotopestudio.backdoor.core.versus.Versus;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.command.ICommand;
import isotopestudio.backdoor.gateway.lobby.Lobby;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientConnectToServer;
import isotopestudio.backdoor.gateway.party.Party;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class PartyCommand extends ICommand {

	@Override
	public void handle(GatewayRemoteClient remoteclient, String[] args) {
		if (args.length == 0) {
			try {
				remoteclient.sendChatMessage("");
				remoteclient.sendChatMessage("/" + getCommand());
				remoteclient.sendChatMessage(" > " + getDescription());
				remoteclient.sendChatMessage("");
				remoteclient.sendChatMessage("Commands:");
				remoteclient
						.sendChatMessage(" - /" + getCommand() + " start | If you want start a game with your lobby");
				remoteclient.sendChatMessage(" - /" + getCommand() + " ready | If you are ready to start a game");
				remoteclient.sendChatMessage(
						" - /" + getCommand() + " unready | If you are no longer ready to start a game");
				remoteclient.sendChatMessage(" - /" + getCommand() + " stop | If you want close your party");
				remoteclient.sendChatMessage(
						" - /" + getCommand() + " search | If you want search a party in the matchmaking");
				remoteclient.sendChatMessage(
						" - /" + getCommand() + " cancelsearch | If you want cancel search a party in the matchmaking");
				remoteclient.sendChatMessage("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args.length == 1) {
			if (!remoteclient.hasLobby()) {
				try {
					remoteclient.sendChatMessage("[ERROR] You are not in any lobby!");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			Lobby lobby = remoteclient.getLobby();
			if (args[0].equalsIgnoreCase("start")) {
				if (lobby.getOwner() != remoteclient) {
					try {
						remoteclient.sendChatMessage("[ERROR] You are not the head of the lobby!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				if (!lobby.allPlayersAreReady()) {
					try {
						remoteclient.sendChatMessage("[ERROR] Not all players are ready!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				if (!lobby.allPlayersAreAvailable()) {
					try {
						remoteclient.sendChatMessage("[ERROR] Not all players are ready!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				for (Versus versus : Versus.values()) {
					if ((lobby.getPlayers().size() / 2) == versus.getMaximum()) {
						GameServerConfiguration configuration = new GameServerConfiguration(Gateway.generatePort(),
								null, false, GameMode.DOMINATION, versus, null, null, null, null, true);
						Party party = new Party(configuration);
						try {
							party.build();
						} catch (Exception e) {
							try {
								remoteclient.sendChatMessage("[ERROR] A problem occurred during the construction of the game server!");
							} catch (IOException exception) {
								exception.printStackTrace();
							}
							e.printStackTrace();
							party.unbuild();
							return;
						}
						party.start();
						try {
							party.connect(lobby.getPlayers());
							remoteclient.sendChatMessage("[SUCCESS] The game has been launched correctly!");
						} catch (IOException exception) {
							exception.printStackTrace();
						}
					}
				}
			} else if (args[0].equals("ready")) {
				lobby.ready(remoteclient.getUser().getUUIDString());
				try {
					remoteclient.sendChatMessage("You are ready.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (args[0].equals("unready")) {
				lobby.unready(remoteclient.getUser().getUUIDString());
				try {
					remoteclient.sendChatMessage("You are no longer ready.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (args[0].equals("stop")) {
				if (lobby.getOwner() != remoteclient) {
					try {
						remoteclient.sendChatMessage("[ERROR] You are not the head of the lobby!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
			} else if (args[0].equals("search")) {
				GameMode gameMode = GameMode.DOMINATION;
				
				Versus versus = null;
				for(Versus versus_ : Versus.values()) {
					if(lobby.getPlayers().size() == versus_.getMaximum()) {
						versus = versus_;
					}
				}
				if (lobby.getOwner() != remoteclient) {
					try {
						remoteclient.sendChatMessage("[ERROR] You are not the head of the lobby!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				if (!lobby.allPlayersAreReady()) {
					try {
						remoteclient.sendChatMessage("[ERROR] Not all players are ready!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				if (!lobby.allPlayersAreAvailable()) {
					try {
						remoteclient.sendChatMessage("[ERROR] Not all players are ready!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				
				if(lobby.getQueue() != null) {
					try {
						remoteclient.sendChatMessage("[ERROR] You are already in a queue!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}

				Gateway.getMatchmaking().joinMatchmakingQueue(lobby, versus, gameMode);
				try {
					lobby.sendMessage("Your lobby is in the queue in "+(gameMode.toString().substring(0, 1).toUpperCase() + gameMode.toString().substring(1).toLowerCase())+" for "+versus.getText()+".");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (args[0].equals("cancelsearch")) {
				if(lobby.getQueue() == null) {
					try {
						remoteclient.sendChatMessage("[ERROR] You are not in any queue!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				
				lobby.getQueue().leave(lobby);
				try {
					lobby.sendMessage("Your lobby is no longer in the queue.");
				} catch (IOException e) {
					e.printStackTrace();
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
		return "party";
	}

	@Override
	public String getDescription() {
		return "Allow you to manage a game party";
	}
}
