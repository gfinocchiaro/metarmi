package it.jugsiracusa.metarmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteView extends Remote{
	
	<T> T viewAs(Class<T> interfaceClass) throws RemoteException;
	

}
