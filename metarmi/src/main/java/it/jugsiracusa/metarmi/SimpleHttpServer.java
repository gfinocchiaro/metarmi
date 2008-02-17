package it.jugsiracusa.metarmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer implements Runnable {

	private class RequestHandler implements Runnable {

		private Socket socket;

		public RequestHandler(Socket socket) {
			this.socket = socket;
		}

		private String parseRequest(InputStream reader) throws IOException {

			byte[] buf = new byte[reader.available()];
			reader.read(buf);

			BufferedReader bufReader = new BufferedReader(new StringReader(
					new String(buf)));

			String line = null;
			String serviceName = null;
			while ((line = bufReader.readLine()) != null) {
				if (line.startsWith("GET")) {
					serviceName = line.split("\\s")[1];

				}
			}
			return serviceName;
		}

		public void run() {
			try {
				OutputStream outputStream = socket.getOutputStream();
				InputStream inputStream = socket.getInputStream();

				String response = "";
				String request = parseRequest(inputStream);

				if (request.startsWith((CONTEXT))) {
					String service = request.split("/")[2];

					if ("list".equals(service)) {
						Set<String> serviceNames = servicesMap.keySet();
						StringBuilder builder = new StringBuilder();
						for (String s : serviceNames) {
							builder.append(s);
							builder.append("\n");
						}
						response = builder.toString();
					} else {

						if (servicesMap.containsKey(service)) {
							response = servicesMap.get(service);
						} else {
							response = "No service " + service
									+ " has available!";
						}
					}
				} else {
					response = "Bad request!";
				}

				PrintWriter writer = new PrintWriter(outputStream);
				writer.println("HTTP/1.0 202 OK");
				writer.println("Server: Metarmi/0.1.0");
				writer.println("Content-Type: text/plain");
				writer.println();
				writer.println(response);
				writer.close();
				inputStream.close();
				socket.close();
			} catch (Throwable io) {
				io.printStackTrace();
			}

		}
	}

	private static final String CONTEXT = "/metarmi/";

	public static void main(String[] args) throws IOException {
		SimpleHttpServer server = new SimpleHttpServer(8081);
		server.mapService("MyService.java", "interface MyService {}");
		server.mapService("YourService.java", "interface YourService {}");
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(server);
		System.out.println("Server started!");
	}

	private ExecutorService clientExecutor;

	private int port;

	private ServerSocket serverSocket;

	private Map<String, String> servicesMap = new HashMap<String, String>();

	public SimpleHttpServer(int port) {
		this.port = port;
		clientExecutor = Executors.newCachedThreadPool();
	}

	public void mapService(String service, String code) {
		servicesMap.put(service, code);
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					clientExecutor.execute(new RequestHandler(socket));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
