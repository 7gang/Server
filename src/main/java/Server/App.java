package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class App {

	public static void main(String[] args) {

		ServerSocket ss;

		try {
			Database db = new Database(/* temporary */);
			ss = new ServerSocket(65432);
			System.out.println("Server listening to port 65432!");

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					try {
						ss.close();
					} catch (IOException e) {
						System.out.println("Server socket could not be gracefully closed!");
						e.printStackTrace();
					}
				}
			}));
			
			while (true) {

				Socket clientSocket = ss.accept();
				clientSocket.setTcpNoDelay(true);
				System.out.println("connection established");
				ConnectionThread ct = new ConnectionThread(db, clientSocket);
				ct.start();

			}
			
		} catch (IOException | SQLException | ClassNotFoundException e) {
			System.out.println("Encountered fatal error: " + e);
			e.printStackTrace();
			System.out.println("Terminating...");
			Runtime.getRuntime().exit(e.hashCode());
		}

	}

}
