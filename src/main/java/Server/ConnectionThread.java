package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

class ConnectionThread extends Thread{

    private Database db;
    private static int number = 0;
    private Socket socket;

    public ConnectionThread(Database db, Socket clientSocket) {

        super("SocketThread#" + number++);
        if (number == Integer.MAX_VALUE)
            number = 0;

        this.socket = clientSocket;
        this.db = db;

    }

    @Override
    public void run() {

        try {
            try {
                ArrayList<String> request = new ArrayList<>();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    
                String line;
                while ((line = in.readLine()) != null)
                    request.add(line);
                
                parseInput(request);
                interrupt();

            } catch (SQLException | IllegalArgumentException e) {
                e.printStackTrace();
                sendFailiure();
                socket.shutdownOutput();
            }
        } catch (IOException e) {
            System.out.println("Communications errored!");
            e.printStackTrace();
            interrupt();
        }

    }

    @Override
    public void interrupt() {

        System.out.println("Killing Thread: " + this.getName());
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Socket could not be gracefully closed!");
            e.printStackTrace();
        }
        super.interrupt();

    }

    private void parseInput(ArrayList<String> request) throws SQLException, IOException, IllegalArgumentException {
        
        System.out.println(request);
        switch(request.size() > 0 ? request.get(0) : "") {

            case "get":
                System.out.println("get request recieved!");
                sendData(db.getData());
                break;

            case "add":
                if (request.size() < 2)
                    throw new IllegalArgumentException();
                db.addQuote(request.get(1));
                sendData(db.getData());
                break;

            case "edit":
                if (request.size() < 3)
                    throw new IllegalArgumentException();
                db.editQuote(request.get(1), request.get(2));
                sendData(db.getData());
                break;

            case "delete":
                if (request.size() < 2)
                    throw new IllegalArgumentException();
                db.deleteQuote(request.get(1));
                sendData(db.getData());
                break;

            default:
                throw new IllegalArgumentException();

        }

    }

    private void sendData(ArrayList<String> data) throws IOException {
        sendSuccess();
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("data");
        for (String line : data)
            out.println(line);
        socket.shutdownOutput();
    }

    private void sendSuccess() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("success");
        out.println("1");
    }

    private void sendFailiure() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("success");
        out.println("0");
    }

}