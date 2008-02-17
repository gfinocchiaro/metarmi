package example.hello;

import it.jugsiracusa.metarmi.RmiExporter;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import example.hello.server.MyGreetings;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.setProperty("java.security.policy","server.policy");
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			
			Registry registry = LocateRegistry.createRegistry(1099);
			
			RmiExporter.get().export(new MyGreetings(), registry);
//			Remote stub = UnicastRemoteObject.exportObject((Remote) new Gr(), 	0);
//			Registry registry = LocateRegistry.createRegistry(1099);
//			registry.bind("greetings", stub);12e
			System.out.println("Server ready");
			
			

		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

	}

}
