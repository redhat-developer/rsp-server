package org.jboss.tools.ssp.client.cli;

import java.util.List;
import java.util.Scanner;

import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerBean;
import org.jboss.tools.ssp.client.bindings.ServerManagementClientLauncher;

public class ServerManagementCLI {

	public static void main(String[] args) {
		ServerManagementCLI cli = new ServerManagementCLI();
		cli.connect(args[0], args[1]);
		cli.readStandardIn();
	}
	
	
	
	
	private final Scanner scanner = new Scanner(System.in);
	private ServerManagementClientLauncher launcher;
	
	public void connect(String host, String port) {
		if( host == null ) {
			System.out.print("Enter server host: ");
			host = scanner.nextLine();
		}
		if( port == null ) {
			System.out.print("Enter server port: ");
			port = scanner.nextLine();
		}
		
		try {
			launcher = new ServerManagementClientLauncher(host, Integer.parseInt(port));
			launcher.launch();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void readStandardIn() {
		while (true) {
			String content = scanner.nextLine();
			try {
				processCommand(content);
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	private void processCommand(String s) throws Exception {
		if( s.equals("list paths") || s.startsWith("list paths ")) {
			List<DiscoveryPath> list = launcher.getServerProxy().getDiscoveryPaths().get();
			System.out.println("Paths:");
			if( list != null ) {
				for( DiscoveryPath dp : list) {
					System.out.println("   " + dp.getFilepath());
				}
			}
		} else if( s.startsWith("add path ")) {
			String suffix = s.substring("add path ".length());
			DiscoveryPath dp = new DiscoveryPath(suffix);
			launcher.getServerProxy().addDiscoveryPath(dp);
		} else if( s.startsWith("remove path ")) {
			String suffix = s.substring("remove path ".length());
			DiscoveryPath dp = new DiscoveryPath(suffix);
			launcher.getServerProxy().removeDiscoveryPath(dp);
		} else if( s.startsWith("search path ")) {
			String suffix = s.substring("remove path ".length());
			DiscoveryPath dp = new DiscoveryPath(suffix);
			List<ServerBean> beans = launcher.getServerProxy().findServerBeans(dp).get();
			System.out.println("Beans:");
			if( beans != null ) {
				for( ServerBean b : beans) {
					System.out.println("   " + b.toString());
				}
			}
			

		} else if( s.startsWith("exit")) {
			launcher.closeConnection();
			System.exit(0);
		} else {
			showCommands();
		}
	}
	
	private void showCommands() {
		// TODO
		System.out.println("Invalid Command");
	}
}
