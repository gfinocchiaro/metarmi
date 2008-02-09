package it.jugsiracusa.metarmi;

import it.jugsiracusa.metarmi.metadata.RemoteService;

import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javassist.ClassPool;
import javassist.CtClass;

public class RmiExporter {

	public static void export(Object remoteObject, Registry registry) {
		String bindName = checkServiceAnnotation(remoteObject);
		exportOnRegistry(remoteObject, bindName, registry);
	}

	public static void export(Object remoteObject, String bindName,
			Registry registry) {
		checkServiceAnnotation(remoteObject);
		exportOnRegistry(remoteObject, bindName, registry);
	}

	private static String checkServiceAnnotation(Object remoteObject) {
		Class<? extends Object> remoteClass = remoteObject.getClass();
		RemoteService rsAnnotation = remoteClass
				.getAnnotation(RemoteService.class);

		if (rsAnnotation == null) {
			throw new IllegalArgumentException(
					"Cannot export a non RemoteService annotated class!");
		}

		String bindName = rsAnnotation.name();
		return bindName;
	}

	private static void exportOnRegistry(Object remoteObject, String bindName,
			Registry registry) {
		try {
			Class<?> remoteClass = remoteObject.getClass();
			ClassPool cp = ClassPool.getDefault();
			String adapterClassName = "it.jugsiracusa.metarmi."
					+ remoteClass.getName() + "_RMIAdapter";
			Class<?> adapterClass = null;
			try {
				adapterClass = Class.forName(adapterClassName);
			} catch (ClassNotFoundException ne) {
				CtClass ra = cp.makeClass(adapterClassName);
				IMemberAppender methodAppender = new AdapterBuilder(ra, adapterClassName).
											addInterface(Remote.class).
											addField(remoteClass, "remoteObject");

				Method[] m = remoteClass.getDeclaredMethods();
				for (Method method : m) {
					methodAppender.addMethod(method);
				}

				adapterClass = methodAppender.
										setConstructor(remoteClass).
										toClass();
			}

			Object rmiAdapter = adapterClass
					.getDeclaredConstructor(remoteClass).newInstance(
							remoteObject);

			/* Exporting object */
			Remote stub = UnicastRemoteObject.exportObject((Remote) rmiAdapter,
					0);

			/* Binding object into the registry */
			registry.bind(bindName, stub);
		} catch (Exception e) {
			throw new RuntimeException(e);

		}
	}

}
