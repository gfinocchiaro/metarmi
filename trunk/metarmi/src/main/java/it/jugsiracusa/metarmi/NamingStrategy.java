package it.jugsiracusa.metarmi;

import example.hello.server.MyGreetings;
import it.jugsiracusa.metarmi.metadata.RemoteService;

public class NamingStrategy {

	public String getRMIAdapterClassName(Class<?> remoteClass) {
		String remoteInterfaceName = remoteClass.getAnnotation(RemoteService.class).target();
		int dot = remoteInterfaceName.lastIndexOf('.');
		String prefix = "";
		if (dot != -1) {
			prefix = remoteInterfaceName.substring(0, dot) + ".";
		}
		return new StringBuilder(prefix).append(remoteClass.getSimpleName()).append("_RMIAdapter").toString();
	}
	
	public String getSimpleInterfaceName(Class<?> remoteClass) {
		String remoteInterfaceName = remoteClass.getAnnotation(RemoteService.class).target();
		int dot = remoteInterfaceName.lastIndexOf('.');
		if (dot!=-1) {
			return remoteInterfaceName.substring(dot+1);
		} else {
			return remoteInterfaceName;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(new NamingStrategy().getSimpleInterfaceName(MyGreetings.class));
	}
}
