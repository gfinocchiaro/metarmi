package example.hello;

import it.jugsiracusa.metarmi.RmiExporter;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import example.hello.server.MyGreetings;
import example.hello.server.OtherGreetings;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			Registry registry = LocateRegistry.createRegistry(1099);
			RmiExporter.export(new MyGreetings(), registry);
			RmiExporter.export(new OtherGreetings(), registry);
			RmiExporter.export(new MyGreetings(), "greetings2", registry);
			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

	}

}
