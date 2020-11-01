package isotopestudio.backdoor.gateway.party;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import doryanbessiere.isotopestudio.commons.LocalDirectory;
import isotopestudio.backdoor.core.server.configuration.GameServerConfiguration;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientConnectToServer;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class Party extends Thread {

	private GameServerConfiguration configuration;
	private ArrayList<GatewayRemoteClient> players = new ArrayList<>();

	public Party(GameServerConfiguration configuration) {
		super();
		this.configuration = configuration;
		Gateway.parties.put(configuration.getSession().toString(), this);
		System.out.println("New party created.");
		System.out.println("configuration=" + configuration.toJson());
	}

	private File directory;
	private File server;

	private Process process;

	public void build() throws MalformedURLException, IOException {
		System.out.println("[PARTY] Creation of a game in progress...");
		System.out.println("[PARTY] configuration=" + configuration.toJson());
		directory = new File(LocalDirectory.toFile(getClass()), "parties/" + configuration.getSession().toString());
		directory.mkdirs();

		server = new File(directory, "server.jar");

		System.out.println("[PARTY] Downloading the last server version...");
		InputStream in = new URL(IsotopeStudioAPI.API_URL + "server/download.php?target="
				+ (Gateway.getVersion().endsWith("SNAPSHOT") ? "snapshot" : "release")).openStream();
		Files.copy(in, Paths.get(server.toURI()), StandardCopyOption.REPLACE_EXISTING);
		System.out.println("[PARTY] Download finish.");
	}

	public void unbuild() {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("[PARTY] Launching the current party...");
		ProcessBuilder builder = new ProcessBuilder("java", "-jar", server.getName(), configuration.toJson());
		builder.directory(directory);

		try {
			process = builder.start();
			try {
				System.out.println("[PARTY] The party is successfully launched (uuid="
						+ configuration.getSession().toString() + ")");
				int code = process.waitFor();
				System.out.println("[PARTY] The party is finish (exit_code=" + code + ")");
				for (GatewayRemoteClient player : players) {
					player.setParty(null);
				}
				// unbuild();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void forceStop() {
		this.process.destroyForcibly();
	}

	/**
	 * @return the players
	 */
	public ArrayList<GatewayRemoteClient> getPlayers() {
		ArrayList<GatewayRemoteClient> players = new ArrayList<>();
		players.addAll(this.players);
		return players;
	}

	/**
	 * @return the process
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * @return the directory
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * @return the configuration
	 */
	public GameServerConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * @param players2
	 * @param address
	 * @param port
	 */
	public void connect(ArrayList<GatewayRemoteClient> players) {
		this.players.addAll(players);
		for (GatewayRemoteClient player : players) {
			player.setParty(this);
		}
		try {
			Gateway.getGatewayServer().sendPacket(new PacketClientConnectToServer(getAddress(), configuration.getPort()),
					players.toArray(new GatewayRemoteClient[1]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	public String getAddress() {
		URL whatismyip;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			String address = in.readLine(); // you get the IP as a String

			return address;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
