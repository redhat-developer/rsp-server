package org.jboss.tools.ssp.client.cli;

import java.util.List;
import java.util.Scanner;

import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerBean;
import org.jboss.tools.ssp.api.beans.VMDescription;
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

	private static final String LIST_VM = "list vm";
	private static final String ADD_VM = "add vm ";
	private static final String REMOVE_VM = "remove vm ";

	private static final String LIST_PATHS = "list paths";
	private static final String ADD_PATH = "add path ";
	private static final String REMOVE_PATH = "remove path ";
	private static final String SEARCH_PATH = "search path ";
	private static final String EXIT = "exit";
	
	private static final String[] CMD_ARR = new String[] {
			LIST_PATHS, ADD_PATH, REMOVE_PATH, SEARCH_PATH, EXIT
	};
	
	private void processCommand(String s) throws Exception {
		if( s.trim().equals(LIST_VM)) {
			List<VMDescription> list = launcher.getServerProxy().getVMs().get();
			System.out.println("VMs:");
			if( list != null ) {
				for( VMDescription d : list ) {
					System.out.println(d.getId() + ": " + d.getVersion() + " @ " + d.getInstallLocation());
				}
			}
		} else if( s.startsWith(ADD_VM)) {
			String suffix = s.substring(ADD_VM.length()).trim();
			int firstSpace = suffix.indexOf(" ");
			launcher.getServerProxy().addVM(
					suffix.substring(0, firstSpace).trim(), 
					suffix.substring(firstSpace).trim());
		} else if( s.startsWith(REMOVE_VM)) {
			String suffix = s.substring(REMOVE_VM.length()).trim();
			launcher.getServerProxy().removeVM(suffix);
		} else if( s.trim().equals(LIST_PATHS)) {
			List<DiscoveryPath> list = launcher.getServerProxy().getDiscoveryPaths().get();
			System.out.println("Paths:");
			if( list != null ) {
				for( DiscoveryPath dp : list) {
					System.out.println("   " + dp.getFilepath());
				}
			}
		} else if( s.startsWith(ADD_PATH)) {
			String suffix = s.substring(ADD_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			launcher.getServerProxy().addDiscoveryPath(dp);
		} else if( s.startsWith(REMOVE_PATH)) {
			String suffix = s.substring(REMOVE_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			launcher.getServerProxy().removeDiscoveryPath(dp);
		} else if( s.startsWith(SEARCH_PATH)) {
			String suffix = s.substring(SEARCH_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			List<ServerBean> beans = launcher.getServerProxy().findServerBeans(dp).get();
			System.out.println("Beans:");
			if( beans != null ) {
				for( ServerBean b : beans) {
					System.out.println("   " + b.toString());
				}
			}
			

		} else if( s.startsWith(EXIT)) {
			launcher.closeConnection();
			System.exit(0);
		} else {
			showCommands();
		}
	}
	
	private void showCommands() {
		System.out.println("Invalid Command");
		System.out.println("Possible commands: ");
		for( int i = 0; i < CMD_ARR.length; i++ ) {
			System.out.println("   " + CMD_ARR[i]);
		}
		
	}
}
