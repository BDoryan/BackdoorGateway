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
import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.party.Party;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class MatchmakingQueue {
	
	private Versus versus;
	private GameMode gameMode;
	
	private ArrayList<Group> waitings = new ArrayList<Group>();
	
	public MatchmakingQueue(Versus versus, GameMode gameMode) {
		super();
		this.versus = versus;
		this.gameMode = gameMode;
	}

	/**
	 * @param lobby2
	 */
	public void join(Group group) {
		int team_count = 2;
		
		@SuppressWarnings("unchecked")
		ArrayList<Group>[] teams = new ArrayList[team_count];
		
		int[] count = new int[teams.length];
		for(int i = 0; i < count.length; i++) {
			teams[i] = new ArrayList<>();
			count[i] = 0;
		}
		
		int index = 0;
		teams[index].add(group);
		count[index] += group.getPlayers().size();
		
		if(group.getPlayers().size() == versus.getMaximum()) {
			index++;
		}
		
		ArrayList<Group> waitings = new ArrayList<>();
		waitings.addAll(this.waitings);

		System.out.println("[QUEUE] Currently in the queue there are "+waitings.size()+" groups");
		for(Group groupInWaiting : waitings) {
			if(groupInWaiting.getPlayers().size() - count[index] == versus.getMaximum()) {
				teams[index].add(groupInWaiting);
				count[index] = versus.getMaximum();
				index++;
				if(index == team_count)break;
			} else {
				teams[index].add(groupInWaiting);
				count[index] += groupInWaiting.getPlayers().size();
			}
		}
		
		if(index == team_count) {
			System.out.println("[QUEUE] A match has been found");
			System.out.println("[QUEUE] Setting up the match");
			GameServerConfiguration configuration = new GameServerConfiguration(Gateway.generatePort(), null, false, gameMode, versus, null, null, null, null, false);

			System.out.println("[QUEUE] Preparing for the match");
			ArrayList<GatewayRemoteClient> players = new ArrayList<>();
			
			for(int i = 0; i < teams.length; i++) {
				for(Group group_ : teams[i]) {
					if(waitings.contains(group_)) 
						waitings.remove(group_);
					if (group_.getQueue() != null)
						group_.setQueue(null);
					
					for(GatewayRemoteClient remoteClient : group_.getPlayers()) {
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

		this.waitings.add(group);
		group.setQueue(this);
		System.out.println("[QUEUE] "+group.getOwner().getUser().getUsername()+"'s group is in the queue.");
	}

	/**
	 * @param lobby2
	 */
	public void leave(Group group) {
		waitings.remove(group);
		group.setQueue(null);
	}
	
	/**
	 * @return the waitings
	 */
	public ArrayList<Group> getWaitings() {
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
