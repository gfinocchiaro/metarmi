package it.jugsiracusa.metarmi;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class RmiAdapter implements RemoteView {

	private Map<Class<?>, Object> interfaces = new HashMap<Class<?>, Object>();

	public RmiAdapter() {
	}

	public void addView(Class<?> interfaceClass, Object implementingInterface) {
		interfaces.put(interfaceClass, implementingInterface);
	}

	public <T> T viewAs(Class<T> interfaceClass) throws RemoteException {
		return (T) interfaces.get(interfaceClass);
	}
}
