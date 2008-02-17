package it.jugsiracusa.metarmi;

import it.jugsiracusa.metarmi.metadata.RemoteService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RmiExporter {

	private static RmiExporter rmiExporter = null;
	
	public static RmiExporter get() {
		if (rmiExporter == null) {
			rmiExporter = new RmiExporter();
		}

		return rmiExporter;

	}
	
	private SimpleHttpServer httpServer = null;
	
	private Executor serverExecutor = Executors.newFixedThreadPool(1);

	private NamingStrategy namingStrategy = new NamingStrategy();
	
	private RmiAnnotationHelper helper = new RmiAnnotationHelper(namingStrategy);

	private String checkServiceAnnotation(Object remoteObject) {
		Class<? extends Object> remoteClass = remoteObject.getClass();
		RemoteService rsAnnotation = remoteClass.getAnnotation(RemoteService.class);

		if (rsAnnotation == null) {
			throw new IllegalArgumentException(
					"Cannot export a non RemoteService annotated class!");
		}

		String bindName = rsAnnotation.name();
		return bindName;
	}

	public void export(Object remoteObject, Registry registry) {
		String bindName = checkServiceAnnotation(remoteObject);
		exportOnRegistry(remoteObject, bindName, registry);
	}

	public void export(Object remoteObject, String bindName, Registry registry) {
		checkServiceAnnotation(remoteObject);
		exportOnRegistry(remoteObject, bindName, registry);
	}

	private void exportOnRegistry(Object remoteObject, String bindName,
			Registry registry) {
		try {
//			startHttpServer();
			Class<?> remoteClass = remoteObject.getClass();
			RemoteService rs = remoteClass.getAnnotation(RemoteService.class);

			
			String adapterClassName = namingStrategy.getRMIAdapterClassName(remoteClass);
			Class<?> adapterClass = null;
			try {
				adapterClass = Class.forName(adapterClassName);
			} catch (ClassNotFoundException ne) {
				adapterClass = helper.createRMIAdapter(remoteClass);
			}

			Object rmiAdapter = adapterClass.newInstance();
			Field field = adapterClass.getDeclaredField("remoteObject");
			field.setAccessible(true);
			field.set(rmiAdapter, remoteObject);

			/* Exporting object */
			Remote stub = UnicastRemoteObject.exportObject((Remote) rmiAdapter, 	0);

			/* Binding object into the registry */
			registry.bind(bindName, stub);
//			httpServer.mapService(namingStrategy.getSimpleInterfaceName(remoteClass)+".java", helper.toCode(Class.forName(rs.target())));
		} catch (Exception e) {
			throw new RuntimeException(e);

		}
	}

	private synchronized void startHttpServer() throws IOException {
		if (httpServer == null) {
			httpServer = new SimpleHttpServer(8081);
			serverExecutor.execute(httpServer);
		}
	}

}
