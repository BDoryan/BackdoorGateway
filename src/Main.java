import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import isotopestudio.backdoor.core.gamemode.GameMode;
import isotopestudio.backdoor.core.server.configuration.GameServerConfiguration;
import isotopestudio.backdoor.core.versus.Versus;
import isotopestudio.backdoor.gateway.Gateway;
import isotopestudio.backdoor.gateway.party.Party;

/**
 * @author BDoryan
 * @github https://www.github.com/BDoryan/
 */
public class Main {

	public static void main(String[] args) {
		try {
			java.io.InputStream is = Gateway.class.getClass().getResourceAsStream("/maven.properties");
			java.util.Properties p = new Properties();
			p.load(is);

			Gateway.version = p.getProperty("VERSION");
		} catch (IOException e) {
			e.printStackTrace();
		}
		GameServerConfiguration configuration = new GameServerConfiguration(Gateway.generatePort(),
				null, false, GameMode.DOMINATION, Versus.ONEvsONE, null, null, null, null, true);
		Party party = new Party(configuration);
		try {
			party.build();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		party.start();
	}
}
