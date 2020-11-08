package isotopestudio.backdoor.gateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import doryanbessiere.isotopestudio.commons.LocalDirectory;
import doryanbessiere.isotopestudio.commons.RunnerUtils;
import doryanbessiere.isotopestudio.commons.logger.Logger;
import doryanbessiere.isotopestudio.commons.logger.file.LoggerFile;
import doryanbessiere.isotopestudio.commons.mysql.SQL;
import doryanbessiere.isotopestudio.commons.mysql.SQLDatabase;
import isotopestudio.backdoor.gateway.group.Group;
import isotopestudio.backdoor.gateway.matckmaking.Matchmaking;
import isotopestudio.backdoor.gateway.packet.packets.PacketClientChatMessage;
import isotopestudio.backdoor.gateway.party.Party;
import isotopestudio.backdoor.gateway.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class Gateway {
	
	public static final String URL_PROFILE_PATERN = "https://isotope-studio.fr/server/api/users/%uuid%/profile.png";

	private static Properties configuration = new Properties();
	public static String version = null;
	private static Logger logger;
	private static SQLDatabase database;
	private static Gson gson = new GsonBuilder().create();
	private static Matchmaking matchmaking = new Matchmaking();
	
	private static GatewayServer server;
	
	private static ArrayList<PacketClientChatMessage> messages = new ArrayList<>();

	public static void main(String[] args) {
		RunnerUtils arguments = new RunnerUtils(args);
		arguments.read();
		try {
			java.io.InputStream is = Gateway.class.getClass().getResourceAsStream("/maven.properties");
			java.util.Properties p = new Properties();
			p.load(is);

			version = p.getProperty("VERSION");
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger = new Logger("Gateway", new LoggerFile(new File(localDirectory(), "logs")));
		System.out.println("Starting the Backdoor Gateway... (version=" + getVersion() + ")");

		System.out.println("Retrieving the gateway configuration...");
		try {
			File configuration_file = new File(localDirectory(), "config.properties");
			if (!configuration_file.exists()) {
				System.err.println("The configuration file cannot be found!");
				System.exit(IsotopeStudioAPI.EXIT_CODE_EXIT);
				return;
			}
			configuration.load(new FileInputStream(configuration_file));
			System.out.println("The configuration file has just been loaded");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Connection to the database...");
		database = new SQLDatabase(SQL.DEFAULT_SQL_DRIVER, SQL.DEFAULT_URLBASE, configuration.getProperty("mysql.url"),
				configuration.getProperty("mysql.database"), configuration.getProperty("mysql.username"),
				configuration.getProperty("mysql.password"));
		if(!database.connect()) {
			System.err.println("Connection to the database failed.!");
			System.exit(IsotopeStudioAPI.EXIT_CODE_EXIT);
			return;
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				if(getCopyParties().size() > 0)
				for(Party party : getCopyParties().values()) {
					party.forceStop();
				}
			}
		}));
		
		server= new GatewayServer(Integer.valueOf(configuration.getProperty("server.port")));
		server.start();
	}
	
	public static HashMap<String, Party> parties = new HashMap<>();
	
	/**
	 * @return the parties
	 */
	public static HashMap<String, Party> getParties() {
		return parties;
	}
	
	/**
	 * @return the copy parties
	 */
	public static HashMap<String, Party> getCopyParties() {
		HashMap<String, Party> parties = new HashMap<>();
		parties.putAll(Gateway.parties);
		return parties;
	}
	
	public static int generatePort() {
		int port = 49152;
		while(true) {
			try {
				Socket socket = new Socket("localhost", port);
				socket.close();
			} catch (Exception e) {
				break;
			}
			port++;
		}
		return port;
	}
	
	public static ArrayList<Group> groups = new ArrayList<>();
	
	/**
	 * @return the lobbies
	 */
	public static ArrayList<Group> getLobbies() {
		return groups;
	}

	/**
	 * @return the copy lobbies
	 */
	public static ArrayList<Group> getCopyLobbies() {
		ArrayList<Group> groups = new ArrayList<>();
		groups.addAll(Gateway.groups);
		return groups;
	}
	
	/**
	 * @return the matchmaking
	 */
	public static Matchmaking getMatchmaking() {
		return matchmaking;
	}
	
	/**
	 * @return the server
	 */
	public static GatewayServer getGatewayServer() {
		return server;
	}
	
	/**
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}

	/**
	 * @return the database
	 */
	public static SQLDatabase getDatabase() {
		return database;
	}

	public static File localDirectory() {
		return LocalDirectory.toFile(Gateway.class);
	}

	/**
	 * @return the gateway server version
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * @return the gateway configuration
	 */
	public static Properties getConfiguration() {
		return configuration;
	}

	/**
	 * @return
	 */
	public static Gson getGson() {
		return gson;
	}
	
	/**
	 * @return the messages
	 */
	public static ArrayList<PacketClientChatMessage> getMessages() {
		return messages;
	}
}
