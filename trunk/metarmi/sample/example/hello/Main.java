package example.hello;

import it.jugsiracusa.metarmi.RmiExporter;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import example.hello.server.Server;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			Server server = new Server();
			Registry registry = LocateRegistry.createRegistry(1099);
			RmiExporter.export(server, registry);
			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

	}

}
