package gateway.isotopestudio.fr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import doryanbessiere.isotopestudio.api.mysql.SQL;
import doryanbessiere.isotopestudio.api.mysql.SQLDatabase;
import doryanbessiere.isotopestudio.commons.LocalDirectory;
import doryanbessiere.isotopestudio.commons.RunnerUtils;
import doryanbessiere.isotopestudio.commons.logger.Logger;
import doryanbessiere.isotopestudio.commons.logger.file.LoggerFile;
import gateway.isotopestudio.fr.server.GatewayServer;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class Gateway {

	private static Properties configuration = new Properties();
	private static String version = null;
	private static Logger logger;
	private static SQLDatabase database;
	
	private static GatewayServer server;

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
		System.out.println("Starting the IsotopeStudio Gateway... (version=" + getVersion() + ")");

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
		
		server= new GatewayServer(Integer.valueOf(configuration.getProperty("server.port")));
		server.start();
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
}
