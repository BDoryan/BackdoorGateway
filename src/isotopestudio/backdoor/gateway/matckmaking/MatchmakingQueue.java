package isotopestudio.backdoor.gateway.matckmaking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import isotopestudio.backdoor.core.gamemode.GameMode;
import isotopestudio.backdoor.core.server.configuration.GameServerConfiguration;
import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.core.versus.Versus;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.lobby.Lobby;
import isotopestudio.backdoor.gateway.party.Party;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class MatchmakingQueue {
	
	private Versus versus;
	private GameMode gameMode;
	
	private ArrayList<Lobby> waitings = new ArrayList<Lobby>();
	
	public MatchmakingQueue(Versus versus, GameMode gameMode) {
		super();
		this.versus = versus;
		this.gameMode = gameMode;
	}

	/**
	 * @param lobby
	 */
	public void join(Lobby lobby) {
		int team_count = 2;
		
		@SuppressWarnings("unchecked")
		ArrayList<Lobby>[] teams = new ArrayList[team_count];
		
		int[] count = new int[teams.length];
		for(int i = 0; i < count.length; i++) {
			teams[i] = new ArrayList<>();
			count[i] = 0;
		}
		
		int index = 0;
		teams[index].add(lobby);
		count[index] += lobby.getPlayers().size();
		
		if(lobby.getPlayers().size() == versus.getMaximum()) {
			index++;
		}
		
		ArrayList<Lobby> waitings = new ArrayList<>();
		waitings.addAll(this.waitings);

		System.out.println("[QUEUE] Currently in the queue there are "+waitings.size()+" lobbies");
		for(Lobby lobbyInWaiting : waitings) {
			if(lobbyInWaiting.getPlayers().size() - count[index] == versus.getMaximum()) {
				teams[index].add(lobbyInWaiting);
				count[index] = versus.getMaximum();
				index++;
				if(index == team_count)break;
			} else {
				teams[index].add(lobbyInWaiting);
				count[index] += lobbyInWaiting.getPlayers().size();
			}
		}
		
		if(index == team_count) {
			System.out.println("[QUEUE] A match has been found");
			System.out.println("[QUEUE] Setting up the match");
			GameServerConfiguration configuration = new GameServerConfiguration(Gateway.generatePort(), null, false, gameMode, versus, null, null, null, null, false);

			System.out.println("[QUEUE] Preparing for the match");
			ArrayList<GatewayRemoteClient> players = new ArrayList<>();
			
			for(int i = 0; i < teams.length; i++) {
				for(Lobby lobby_ : teams[i]) {
					if(waitings.contains(lobby_)) 
						waitings.remove(lobby_);
					if (lobby_.getQueue() != null)
						lobby_.setQueue(null);
					
					for(GatewayRemoteClient remoteClient : lobby_.getPlayers()) {
						String uuid = remoteClient.getUser().getUUIDString();
						configuration.getWhitelist().add(uuid);
						configuration.getTeamAssigned().put(uuid, Team.values()[i]);	
						players.add(remoteClient);
					}
				}
			}
			
			Party party = new Party(configuration);
			try {
				party.build();
			} catch (Exception e) {
				e.printStackTrace();
				party.unbuild();
			}
			party.start();
			System.out.println("[QUEUE] Starting the match");
			party.connect(players);
			System.out.println("[QUEUE] Connecting players to the game");
			return;
		}

		this.waitings.add(lobby);
		lobby.setQueue(this);
		System.out.println("[QUEUE] "+lobby.getOwner().getUser().getUsername()+"'s lobby is in the queue.");
	}

	/**
	 * @param lobby
	 */
	public void leave(Lobby lobby) {
		waitings.remove(lobby);
		lobby.setQueue(null);
	}
	
	/**
	 * @return the waitings
	 */
	public ArrayList<Lobby> getWaitings() {
		return waitings;
	}
	
	/**
	 * @return the gameMode
	 */
	public GameMode getGameMode() {
		return gameMode;
	}
	
	/**
	 * @return the versus
	 */
	public Versus getVersus() {
		return versus;
	}
}
