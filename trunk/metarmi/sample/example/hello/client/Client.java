package example.hello.client;

import java.lang.reflect.Method;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

	private Client() {
	}

	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		String host = (args.length < 1) ? null : args[0];
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			Object remoteObject = registry.lookup("greetings");
			Method method = remoteObject.getClass().getDeclaredMethod(
					"sayGoodBye", null);
			String response = (String) method.invoke(remoteObject, null);
			System.out.println("response: " + response);
			
			remoteObject = registry.lookup("otherGreetings");
			method = remoteObject.getClass().getDeclaredMethod(
					"sayHello", null);
			response = (String) method.invoke(remoteObject, null);
			System.out.println("response: " + response);
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
