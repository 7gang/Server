package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class App {

	public static void main(String[] args) {

		ServerSocket ss; // define the server socket

		try {
			Database db = new Database(/* temporary */); // initialize database module
			ss = new ServerSocket(65432); // open new server socket at port 65432 (unassigned port)
			System.out.println("Server listening to port 65432!");

			// define a new cleanup Thread to run when the program is terminated, attempting to close the server socket
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					try {
						ss.close(); // closing attempt
					} catch (IOException e) {
						// disrigard any errors
						System.out.println("Server socket could not be gracefully closed!");
						e.printStackTrace();
					}
				}
			}));
			
			while (true) { // infinite loop

				Socket clientSocket = ss.accept(); // execution is halted at this line until a client attempts to establish a connection
				// following a client connection attempt, setup the socket and hand it over to a newly instantiated ConnectionThread
				clientSocket.setTcpNoDelay(true);
				System.out.println("connection established");
				ConnectionThread ct = new ConnectionThread(db, clientSocket);
				ct.start(); // begin execution of ConnectionThread to handle client network request

			}
			
		} catch (IOException | SQLException | ClassNotFoundException e) {
			// if any error happens, panic and quit
			System.out.println("Encountered fatal error: " + e);
			e.printStackTrace();
			System.out.println("Terminating...");
			Runtime.getRuntime().exit(e.hashCode());
		}

	}

}
