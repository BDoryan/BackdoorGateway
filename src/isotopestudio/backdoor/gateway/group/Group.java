package isotopestudio.backdoor.gateway.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.matckmaking.MatchmakingQueue;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class Group {

	private GatewayRemoteClient owner;
	private boolean isPrivate = true;

	private ArrayList<GatewayRemoteClient> players = new ArrayList<>();
	private ArrayList<String> whitelist = new ArrayList<>();
	private ArrayList<String> playersReady = new ArrayList<>();

	private MatchmakingQueue queue;

	public Group(GatewayRemoteClient owner, boolean isPrivate) {
		this.owner = owner;
		this.isPrivate = isPrivate;
		this.players.add(owner);
		Gateway.getLobbies().add(this);
		
		this.owner.setGroup(this);
		System.out.println("New group created by " + owner.getUser().getUsername() + ".");
	}

	public void ready(String uuid) {
		playersReady.add(uuid);
	}

	public boolean isReady(String uuid) {
		return playersReady.contains(uuid);
	}
	
	public boolean allPlayersAreReady() {
		for(GatewayRemoteClient player : getPlayers())
		{
			if(player != owner && !playersReady.contains(player.getUser().getUUIDString())) return false;
		}
		return true;
	}

	public boolean allPlayersAreAvailable() {
		for(GatewayRemoteClient player : getPlayers())
		{
			if(player.inParty()) return false;
		}
		return true;
	}
	
	public void unready(String uuid) {
		playersReady.remove(uuid);
	}
	
	public void whitelist(UUID uuid) {
		whitelist(uuid.toString());
	}

	public void whitelist(String uuidString) {
		if (!isPrivate())
			setPrivate(true);
		whitelist.add(uuidString);
	}

	public void unwhitelist(UUID uuid) {
		unwhitelist(uuid.toString());
	}

	public void unwhitelist(String uuidString) {
		whitelist.remove(uuidString);
	}
	
	/**
	 * @return the whitelist
	 */
	public ArrayList<String> getWhitelist() {
		return whitelist;
	}

	/**
	 * @param queue the queue to set
	 */
	public void setQueue(MatchmakingQueue queue) {
		this.queue = queue;
	}

	/**
	 * @return the queue
	 */
	public MatchmakingQueue getQueue() {
		return queue;
	}

	/**
	 * @return the players
	 */
	public ArrayList<GatewayRemoteClient> getPlayers() {
		ArrayList<GatewayRemoteClient> players = new ArrayList<>();
		if(this.players.size() == 0) 
			return players;
		
		players.addAll(this.players);
		return players;
	}

	public boolean join(GatewayRemoteClient client) {
		if (isPrivate() && !whitelist.contains(client.getUser().getUUIDString())) {
			return false;
		}
		if(isPrivate() && whitelist.contains(client.getUser().getUUIDString())) {
			this.whitelist.remove(client.getUser().getUUIDString());
		}
		this.players.add(client);
		client.setGroup(this);
		System.out.println(
				client.getUser().getUsername() + " has just joined " + owner.getUser().getUsername() + " group.");
		try {
			sendMessage(client.getUser().getUsername() + " has just joined the group.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean kick(GatewayRemoteClient client) {
		if (client.equals(owner)) {
			return false;
		}
		if(!getPlayers().contains(client)) {
			return false;
		}
		this.players.remove(client);
		client.setGroup(null);
		System.out.println(client.getUser().getUsername()+" has just been kicked from "+owner.getUser().getUsername()+" group.");
		try {
			sendMessage(client.getUser().getUsername() + " has just been kicked from the group");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void leave(GatewayRemoteClient client) {
		if (client.equals(owner)) {
			destroy();
			return;
		}
		this.players.remove(client);
		client.setGroup(null);
		System.out.println(
				client.getUser().getUsername() + " has just left " + owner.getUser().getUsername() + " group.");
		try {
			sendMessage(client.getUser().getUsername() + " has just left the group");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete the lobby
	 */
	public void destroy() {
		if (getQueue() != null)
			getQueue().leave(this);
		Gateway.getLobbies().remove(this);

		if(getPlayers() != null && getPlayers().size() > 0) {
			for(GatewayRemoteClient remoteClient : getPlayers()) {
				getPlayers().remove(remoteClient);
				remoteClient.setGroup(null);
			}	
		}
		System.out.println("Removed " + owner.getUser().getUsername() + " group.");
		
		try {
			sendMessage(owner.getUser().getUsername()+" has just removed the group");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) throws IOException {
		for(GatewayRemoteClient remoteClient : getPlayers()) {
			if(remoteClient.isConnected())
			remoteClient.sendChatMessage(message);
		}
	}
	
	/**
	 * @param isPrivate the private to set
	 */
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	/**
	 * @return true if the is in private also false
	 */
	public boolean isPrivate() {
		return isPrivate;
	}

	/**
	 * @return the owner
	 */
	public GatewayRemoteClient getOwner() {
		return owner;
	}
}
