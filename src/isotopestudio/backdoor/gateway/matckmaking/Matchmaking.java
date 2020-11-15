package isotopestudio.backdoor.gateway.matckmaking;

import java.util.ArrayList;

import isotopestudio.backdoor.core.gamemode.GameMode;
import isotopestudio.backdoor.core.versus.Versus;
import isotopestudio.backdoor.gateway.group.Group;

/**
 * @author BDoryan
 * @github https://www.github.com/BDoryan/
 */
public class Matchmaking {
	
	private ArrayList<MatchmakingQueue> queues = new ArrayList<>();

	public Matchmaking() {
		for(Versus versus : Versus.values()) {
			for(GameMode gameMode : GameMode.values()) {
				MatchmakingQueue matchmakingQueue = new MatchmakingQueue(versus, gameMode);
				queues.add(matchmakingQueue);
			}
		}
	}
	
	public void joinMatchmakingQueue(Group group, Versus versus, GameMode gameMode) {
		if(group.getPlayers().size() > versus.getMaximum())
			return;
		
		MatchmakingQueue matchmakingQueue = getMatchmakingQueue(versus, gameMode);
		matchmakingQueue.join(group);
	}
	
	public MatchmakingQueue getMatchmakingQueue(Versus versus, GameMode gameMode) {
		for(MatchmakingQueue matchmakingQueue : queues) 
			if(matchmakingQueue.getGameMode() == gameMode && matchmakingQueue.getVersus() == versus) 
				return matchmakingQueue;
		return null;
	}
}
