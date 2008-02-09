package example.hello.server;

import example.hello.Greetings;
import it.jugsiracusa.metarmi.metadata.RemoteMethod;
import it.jugsiracusa.metarmi.metadata.RemoteService;

@RemoteService(name = "greetings")
public class MyGreetings {

	public MyGreetings() {
	}

	@RemoteMethod(targetInterface = Greetings.class)
	public String sayHello() {
		return "Hello, world!";
	}

	@RemoteMethod(name = "sayGoodBye", targetInterface = Greetings.class)
	public String goodBye() {
		return "Good bye!";
	}

}
