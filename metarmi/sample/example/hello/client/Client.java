package example.hello.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class Client {

	private Client() {
	}

	public static void showDeclaredInterfaces(Object obj) {
		System.out.println(Arrays.deepToString(obj.getClass().getInterfaces()));
	}

	public static void main(String[] args) {
		System.setProperty("java.rmi.server.codebase","file:../metarmi/generated/");
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		String host = (args.length < 1) ? null : args[0];
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			Object remoteObject = registry.lookup("greetings");
			
			showDeclaredInterfaces(remoteObject);
			
			invoke(remoteObject, "sayGoodBye");
			invoke(remoteObject, "sayHello");
//			System.out.println(gr.sayHello());
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}
	public static void main2(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		String host = (args.length < 1) ? null : args[0];
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			Object remoteObject = registry.lookup("greetings");
			showDeclaredInterfaces(remoteObject);
//			invoke(remoteObject, "sayGoodBye");
			invoke(remoteObject, "sayHello");
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private static void invoke(Object remoteObject, String methodName)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Method method = remoteObject.getClass().getDeclaredMethod(methodName,
				null);
		String response = (String) method.invoke(remoteObject, null);
		System.out.println("response: " + response);
	}
}
