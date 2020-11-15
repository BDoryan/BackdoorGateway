package isotopestudio.backdoor.gateway.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import isotopestudio.backdoor.core.gamemode.GameMode;
import isotopestudio.backdoor.core.versus.Versus;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.matckmaking.MatchmakingQueue;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientReceiveNotification;
import isotopestudio.backdoor.gateway.packet.packets.group.PacketGroupMessage;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BDoryan
 * @github https://www.github.com/BDoryan/
 */
public class Group {

	private UUID uuid;

	private GatewayRemoteClient owner;
	private boolean isPrivate = true;

	private ArrayList<GatewayRemoteClient> players = new ArrayList<>();
	private ArrayList<String> whitelist = new ArrayList<>();
	private ArrayList<String> playersReady = new ArrayList<>();

	private MatchmakingQueue queue;

	private GameMode gamemode = GameMode.DOMINATION;
	private Versus versus = Versus.ONEvsONE;

	public Group(GatewayRemoteClient owner, boolean isPrivate) {
		this.uuid = UUID.randomUUID();
		this.owner = owner;
		this.isPrivate = isPrivate;
		this.players.add(owner);
		Gateway.getLobbies().add(this);

		this.owner.setGroup(this);
		System.out.println("New group created by " + owner.getUser().getUsername() + ".");
	}

	public void ready(String uuid) {
		playersReady.add(uuid);
		update();
	}

	public void unready(String uuid) {
		if (owner.getUser().getUUIDString().equals(uuid))
			return;
		playersReady.remove(uuid);
		update();
	}

	public boolean isReady(String uuid) {
		return playersReady.contains(uuid);
	}

	public boolean allPlayersAreReady() {
		for (GatewayRemoteClient player : getPlayers()) {
			if (player != owner && !playersReady.contains(player.getUser().getUUIDString()))
				return false;
		}
		return true;
	}

	public boolean allPlayersAreAvailable() {
		for (GatewayRemoteClient player : getPlayers()) {
			if (player.inParty())
				return false;
		}
		return true;
	}

	private Thread message_timer;

	public void message(String type, String message, long ms) {
		if (message_timer != null && message_timer.isAlive()) {
			message_timer.destroy();
		}
		this.message_timer = new Thread(new Runnable() {
			@Override
			public void run() {
				players.forEach((client) -> {
					try {
						client.sendPacket(new PacketGroupMessage(type, message));

						if (ms < 0)
							return;
						try {
							long timeLeft = System.currentTimeMillis() + ms;
							while(System.currentTimeMillis() < timeLeft) {}
							client.sendPacket(new PacketGroupMessage(type, ""));
						} catch (IOException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		});
		this.message_timer.start();
	}

	public void whitelist(UUID uuid) {
		whitelist(uuid.toString());
	}

	public void whitelist(String uuidString) {
		whitelist.add(uuidString);
		update();
	}

	public void unwhitelist(UUID uuid) {
		unwhitelist(uuid.toString());
	}

	public void unwhitelist(String uuidString) {
		whitelist.remove(uuidString);
		update();
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
		update();
	}

	/**
	 * @return the versus
	 */
	public Versus getVersus() {
		return versus;
	}

	/**
	 * @return the gamemode
	 */
	public GameMode getGamemode() {
		return gamemode;
	}

	public GroupObject getGroupObject() {
		GroupObject groupObject = new GroupObject(getQueue() != null, gamemode, versus, getUUID().toString(),
				owner.getProfile(), isPrivate);
		groupObject.getPlayersReady().addAll(playersReady);
		groupObject.getWhitelist().addAll(whitelist);
		getPlayers().forEach((remoteClient) -> groupObject.getPlayers().add(remoteClient.getProfile()));

		return groupObject;
	}

	public void update() {
		getPlayers().forEach((remoteClient) -> remoteClient.updateGroup(this));
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
		update();
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
		if (this.players.size() == 0)
			return players;

		players.addAll(this.players);
		return players;
	}

	public boolean join(GatewayRemoteClient client) {
		if (!whitelist.contains(client.getUser().getUUIDString())) {
			return false;
		}
		this.whitelist.remove(client.getUser().getUUIDString());
		this.players.add(client);
		update();

		client.setGroup(this);
		System.out.println(
				client.getUser().getUsername() + " has just joined " + owner.getUser().getUsername() + " group.");
		try {
			sendChatMessage(client.getUser().getUsername() + " has just joined the group.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean kick(GatewayRemoteClient client) {
		if (client.equals(owner)) {
			return false;
		}
		if (!getPlayers().contains(client)) {
			return false;
		}
		this.players.remove(client);
		update();

		client.setGroup(null);
		System.out.println(client.getUser().getUsername() + " has just been kicked from "
				+ owner.getUser().getUsername() + " group.");
		try {
			sendChatMessage(client.getUser().getUsername() + " has just been kicked from the group");
			client.sendPacket(new PacketClientReceiveNotification("group_kick", "lang:group_you_have_been_kicked_title",
					"lang:group_you_have_been_kicked_message"));
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
		update();

		if (isReady(client.getUser().getUUIDString())) {
			playersReady.remove(client.getUser().getUUIDString());
		}

		client.setGroup(null);
		System.out.println(
				client.getUser().getUsername() + " has just left " + owner.getUser().getUsername() + " group.");
		try {
			sendChatMessage(client.getUser().getUsername() + " has just left the group");
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

		if (getPlayers() != null && getPlayers().size() > 0) {
			for (GatewayRemoteClient remoteClient : getPlayers()) {
				getPlayers().remove(remoteClient);
				remoteClient.setGroup(null);
				try {
					remoteClient.sendPacket(new PacketClientReceiveNotification("group_deleted",
							"lang:group_your_group_has_been_deleted_title",
							"lang:group_your_group_has_been_deleted_message"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Removed " + owner.getUser().getUsername() + " group.");

		try {
			sendChatMessage(owner.getUser().getUsername() + " has just removed the group");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendChatMessage(String message) throws IOException {
		for (GatewayRemoteClient remoteClient : getPlayers()) {
			if (remoteClient.isConnected())
				remoteClient.sendChatMessage(message);
		}
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

	/**
	 * @return the uuid
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * @return
	 */
	public boolean isOverload() {
		return isPrivate ? getPlayers().size() > (versus.getMaximum() + versus.getMaximum())
				: (getPlayers().size() > versus.getMaximum());
	}

	/**
	 * @return
	 */
	public boolean isFull() {
		return isPrivate ? getPlayers().size() >= (versus.getMaximum() + versus.getMaximum())
				: getPlayers().size() >= versus.getMaximum();
	}

	/**
	 * @param isPrivate2
	 * @param versus2
	 * @param gamemode2
	 */
	public void set(boolean isPrivate, Versus versus, GameMode gamemode) {
		System.out.println(isPrivate + ", " + versus.getText() + ", " + gamemode.toString());
		this.isPrivate = isPrivate;
		this.versus = versus;
		this.gamemode = gamemode;
		update();
	}
}
