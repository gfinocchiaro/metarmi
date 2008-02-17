package example.hello.server;

import it.jugsiracusa.metarmi.metadata.RemoteMethod;
import it.jugsiracusa.metarmi.metadata.RemoteService;

import java.io.Serializable;

@RemoteService(name = "greetings", target="example.hello.server.Greetings")
public class MyGreetings implements Serializable {

	public MyGreetings() {
	}

	@RemoteMethod(name = "sayHello")
	public String hello() {
		return "hello";

	}

	@RemoteMethod(name = "sayGoodBye")
	public String goodBye() {
		return "Good bye!";
	}

	private void foo() {

	}

}
