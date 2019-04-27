package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A seperate thread that handles a client query from a TCP socket
 */
class ConnectionThread extends Thread{

    private Database db; // the database interface
    private static int number = 0; // the number id associated with this Thread
    private Socket socket; // the socket connection to the client

    /**
     * Initialize a new ConnectionThread object
     * @param db the database object
     * @param clientSocket the socket connection
     */
    public ConnectionThread(Database db, Socket clientSocket) {

        // establish the number of this ConnetionThread instance, catching a potential overflow
        super("SocketThread#" + number++);
        if (number == Integer.MAX_VALUE)
            number = 0;

        // initialize the socket connection and database interface
        this.socket = clientSocket;
        this.db = db;

    }

    @Override
    public void run() {

        try {
            try {
                // attempt to read the socket input and parse it
                ArrayList<String> request = new ArrayList<>(); // ArrayList to hold the request input, line by line
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // create a new input stream
    
                // iterate over incomming lines, adding them to the request ArrayList
                String line;
                while ((line = in.readLine()) != null)
                    request.add(line);
                
                // attempt to parse the request
                parseInput(request); // will throw an exeption if unsuccessful, preventing termination
                interrupt();

            } catch (SQLException | IllegalArgumentException e) {
                // if the request could not be processed because of reasons not to do with the network connection, attempt to send back a failiure response
                e.printStackTrace();
                sendFailiure();
                socket.shutdownOutput();
            }
        } catch (IOException e) {
            // if a network exception interrupted execution, give up and kill the Thread
            System.out.println("Communications errored!");
            e.printStackTrace();
            interrupt();
        }

    }

    @Override
    public void interrupt() {

        // attempt to clean up Thread by closing the socket connection before interrupting
        System.out.println("Killing Thread: " + this.getName());
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Socket could not be gracefully closed!");
            e.printStackTrace();
        }
        super.interrupt();

    }

    /**
     * Attempts to parse the given ArrayList of TCP input lines, handling success-cases but not failiures
     * @param request the ArrayList of input lines from the TCP connection
     * @throws SQLException in case the database was unable to perform the desired task
     * @throws IOException in case the response could not be sent to the client
     * @throws IllegalArgumentException in case the client formatted the request inproperly
     */
    private void parseInput(ArrayList<String> request) throws SQLException, IOException, IllegalArgumentException {
        
        System.out.println(request);
        switch(request.size() > 0 ? request.get(0) : "") { // attempt to parse the request, falling back on the default scenario if no lines were supplied

            case "get":
                // parse the request as a "get" request, returning the dataset
                System.out.println("get request recieved!");
                sendData(db.getData());
                break;

            case "add":
                // parse the request as an "add" request
                if (request.size() < 2)
                    // propagate error if no quote was supplied
                    throw new IllegalArgumentException();
                // add the supplied quote to the db
                db.addQuote(request.get(1));
                // send back the updated table
                sendData(db.getData());
                break;

            case "edit":
                // parse the request as an "edit" request
                if (request.size() < 3)
                    // propagate the error if no target quote or new quote was supplied
                    throw new IllegalArgumentException();
                // ask the db to edit the target quote with the new quote
                db.editQuote(request.get(1), request.get(2));
                // send back the updated table
                sendData(db.getData());
                break;

            case "delete":
                // parse the request as a "delete" request
                if (request.size() < 2)
                    // propagate the error if no quote was supplied
                    throw new IllegalArgumentException();
                // ask the db to delete the given quote
                db.deleteQuote(request.get(1));
                // return the updated table
                sendData(db.getData());
                break;

            default:
                // throw an error if the supplied lines did not correlate to any supported action
                throw new IllegalArgumentException();

        }

    }

    /**
     * Attempt to send the given data over the network
     * @param data the data to be sent
     * @throws IOException in case the data could not be sent
     */
    private void sendData(ArrayList<String> data) throws IOException {
        sendSuccess(); // start by sending the success response to the client
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // create a new output stream
        out.println("data"); // send a "data" line, declaring that the following lines will be the database table, where 1 line = 1 quote
        for (String line : data)
            out.println(line); // send each data point over the wire
        socket.shutdownOutput(); // end the request by closing the output stream
    }

    /**
     * Attempt to send a success response
     * @throws IOException in case the success response could not be sent
     */
    private void sendSuccess() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // create a new output stream
        // send the success response code
        out.println("success");
        out.println("1");
    }

    /**
     * Attempt to send a failiure response
     * @throws IOException in case the failiure response could not be sent
     */
    private void sendFailiure() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // create a new output stream
        // send the failiure response code
        out.println("success");
        out.println("0");
    }

}