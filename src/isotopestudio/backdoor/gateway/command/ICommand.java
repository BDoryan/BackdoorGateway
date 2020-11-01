package isotopestudio.backdoor.gateway.command;

import java.util.Scanner;

import isotopestudio.backdoor.gateway.command.commands.LobbyCommand;
import isotopestudio.backdoor.gateway.command.commands.NotificationCommand;
import isotopestudio.backdoor.gateway.command.commands.PartyCommand;
import isotopestudio.backdoor.gateway.server.GatewayRemoteClient;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public abstract class ICommand {
	public static ICommand[] commands = new ICommand[] {
		new NotificationCommand(),
		new LobbyCommand(),
		new PartyCommand()
	};

	public abstract void handle(GatewayRemoteClient remoteclient, String[] args);

	public abstract String getCommand();

	public abstract String getDescription();

	/**
	 * 
	 * TODO pensez au système de permission ou de level
	 * 
	 * @param line
	 * @return
	 */
	public static boolean command(GatewayRemoteClient remoteclient, String line) {
		String[] args = line.split(" ");
		String target = args[0];

		System.out.println(line);

		for (ICommand command : commands) {
			if (command.getCommand().equalsIgnoreCase(target)) {
				String[] arguments = new String[args.length - 1];
				if (args.length > 1) {
					for (int i = 1; i < args.length; i++) {
						arguments[i - 1] = args[i];
					}
				}
				command.handle(remoteclient, arguments);
				return true; 
			}
		}
		System.err.println("This command is unknown");
		return false;
	}
}
