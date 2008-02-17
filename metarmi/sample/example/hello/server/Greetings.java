package example.hello.server;

public interface Greetings extends java.rmi.Remote {

	java.lang.String sayHello() throws java.rmi.RemoteException;

	java.lang.String sayGoodBye() throws java.rmi.RemoteException;

}
