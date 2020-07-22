package gateway.isotopestudio.fr.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import doryanbessiere.isotopestudio.api.authentification.User;
import doryanbessiere.isotopestudio.api.mysql.SQLDatabase;
import gateway.isotopestudio.fr.Gateway;

/**
 * @author BESSIERE
 * @github https://www.github.com/DoryanBessiere/
 */
public class GatewayClient extends Thread {

	private User user;
	private Socket socket;

	private DataOutputStream output;
	private DataInputStream input;
	
	private boolean connected;
	private boolean authenticated;

	public void connect(Socket socket) throws IOException, SQLException {
		this.socket = socket;
		this.output = new DataOutputStream(this.socket.getOutputStream());
		this.input = new DataInputStream(this.socket.getInputStream());
		connected = true;
		start();
	}
	
	@Override
	public void run() {
		SQLDatabase database = Gateway.getDatabase();
		
		try {
			String[] authentification = input.readLine().split(";");
			if (database.has("users", "email", authentification[0])) {
				if(database.getString("users", "email", authentification[0], "token").equals(authentification[1])) {
					authenticated = true;
					this.user = User.fromJson(database.getString("users_data", "uuid", database.getString("users", "email", authentification[0], "uuid"), "json"));
					Gateway.getGatewayServer().clients.add(this);
					return;
				}
			}
			kick("authentification_invalid");
			return;
		} catch (IOException | SQLException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("The "+user.getUsername()+"[uuid="+user.getUUIDString()+"] user is logged into the gateway :D");
		while(connected && authenticated) {
			try {
				String inputString = input.readLine();
			} catch (IOException e) {
				connected = false;
			}
		}
		disconnected();
	}
	
	public void kick(String reason) {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
	}
	
	public void disconnected() {
		System.out.println("The "+user.getUsername()+"[uuid="+user.getUUIDString()+"] user is no longer logged on to the gateway. ");
		Gateway.getGatewayServer().clients.remove(this);
		try {
			socket.close();
		} catch (Exception e) {
		}
	}
	
	/**
	 * @return the connection status
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * @return the authenticated
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}
}
