package Server;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


class ServerThread extends Thread {
    @Override
    public void run() {
        App.main(new String[] {});
    }
}


public class AppTest {

    private static ServerThread st;
    private static Database db;
    // private static ArrayList<String> previous = new ArrayList<>();

    @BeforeClass
    public static void before() throws InterruptedException, ClassNotFoundException, SQLException {
        db = new Database();
        st = new ServerThread();
        st.start();
        Thread.sleep(2000); // give the server time to initialize
    }

    @Test(timeout = 2000)
    public void testAppGetData() throws UnknownHostException, IOException {

        // establish client connection
        ArrayList<String> response = new ArrayList<>();
        Socket socket = new Socket("localhost", 65432);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send request
        out.println("get");
        socket.shutdownOutput();

        // read response
        while (response.size() == 0) {
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.add(inputLine);
        }

        System.out.println(response);

        // verify response
        assertEquals("success", response.get(0));
        assertEquals("1", response.get(1));
        assertEquals("data", response.get(2));

        // close connection
        out.close();
        in.close();
        socket.close();

    }

    @Test(timeout = 2000)
    public void testAppAddQuote() throws UnknownHostException, IOException, SQLException {

        // establish client connection
        ArrayList<String> response = new ArrayList<>();
        Socket socket = new Socket("localhost", 65432);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send request
        out.println("add");
        out.println("an amazing quote");
        socket.shutdownOutput();

        // read response
        while (response.size() == 0) {
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.add(inputLine);
        }

        System.out.println(response);

        // verify response
        assertEquals("success", response.get(0));
        assertEquals("1", response.get(1));
        assertEquals("data", response.get(2));
        assertEquals("an amazing quote", response.get(2 + db.getData().size()));

        // close connection
        out.close();
        in.close();
        socket.close();

    }

    @Test(timeout = 2000)
    public void testAppEditQuote() throws UnknownHostException, IOException, SQLException {

        // establish client connection
        ArrayList<String> response = new ArrayList<>();
        Socket socket = new Socket("localhost", 65432);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send add request
        out.println("add");
        out.println("quote with typo");

        // close connection
        out.close();
        in.close();
        socket.close();

        // establish new client connection
        response = new ArrayList<>();
        socket = new Socket("localhost", 65432);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send edit request
        out.println("edit");
        out.println("quote with typo");
        out.println("quote without typo");
        socket.shutdownOutput();

        // read response
        while (response.size() == 0) {
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.add(inputLine);
        }

        System.out.println(response);

        // verify response
        assertEquals("success", response.get(0));
        assertEquals("1", response.get(1));
        assertEquals("data", response.get(2));
        assertEquals("quote without typo", response.get(2 + db.getData().size()));

        // close connection
        out.close();
        in.close();
        socket.close();

    }

    @Test(timeout = 2000)
    public void testAppEditQuoteException() throws UnknownHostException, IOException, SQLException {

        // establish client connection
        ArrayList<String> response = new ArrayList<>();
        Socket socket = new Socket("localhost", 65432);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send add request
        out.println("add");
        out.println("another quote with typo");

        // close connection
        out.close();
        in.close();
        socket.close();

        // establish new client connection
        response = new ArrayList<>();
        socket = new Socket("localhost", 65432);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send edit request
        out.println("edit");
        out.println("quote not found in db");
        out.println("quote without typo");
        socket.shutdownOutput();

        // read response
        while (response.size() == 0) {
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.add(inputLine);
        }

        System.out.println(response);

        // verify response
        assertEquals("success", response.get(0));
        assertEquals("0", response.get(1));
        assertEquals(2, response.size());

        // close connection
        out.close();
        in.close();
        socket.close();

    }

    @Test(timeout = 2000)
    public void testAppDeleteQuote() throws UnknownHostException, IOException, SQLException {

        // establish client connection
        ArrayList<String> response = new ArrayList<>();
        Socket socket = new Socket("localhost", 65432);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send add request
        out.println("add");
        out.println("a bad quote");

        // close connection
        out.close();
        in.close();
        socket.close();

        ArrayList<String> oldDB = db.getData();

        // establish new client connection
        response = new ArrayList<>();
        socket = new Socket("localhost", 65432);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send edit request
        out.println("delete");
        out.println("a bad quote");
        socket.shutdownOutput();

        // read response
        while (response.size() == 0) {
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.add(inputLine);
        }

        System.out.println(response);

        // verify response
        assertEquals("success", response.get(0));
        assertEquals("1", response.get(1));
        assertEquals("data", response.get(2));
        assertEquals(oldDB, db.getData());

        // close connection
        out.close();
        in.close();
        socket.close();

    }

    @Test(timeout = 2000)
    public void testAppDeleteQuoteException() throws UnknownHostException, IOException, SQLException {

        // establish client connection
        ArrayList<String> response = new ArrayList<>();
        Socket socket = new Socket("localhost", 65432);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send add request
        out.println("add");
        out.println("another bad quote");

        // close connection
        out.close();
        in.close();
        socket.close();

        // establish new client connection
        response = new ArrayList<>();
        socket = new Socket("localhost", 65432);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send edit request
        out.println("delete");
        out.println("quote not found in db");
        socket.shutdownOutput();

        // read response
        while (response.size() == 0) {
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.add(inputLine);
        }

        System.out.println(response);

        // verify response
        assertEquals("success", response.get(0));
        assertEquals("0", response.get(1));
        assertEquals(2, response.size());

        // close connection
        out.close();
        in.close();
        socket.close();

    }

    @Test(timeout = 2000)
    public void testAppBadFormat() throws UnknownHostException, IOException, SQLException {

        // establish client connection
        ArrayList<String> response = new ArrayList<>();
        Socket socket = new Socket("localhost", 65432);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send add request
        out.println("jibberish");
        socket.shutdownOutput();

        // read response
        while (response.size() == 0) {
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.add(inputLine);
        }

        System.out.println(response);

        // verify response
        assertEquals("success", response.get(0));
        assertEquals("0", response.get(1));
        assertEquals(2, response.size());

        // close connection
        out.close();
        in.close();
        socket.close();

    }

    @AfterClass
    public static void after() throws InterruptedException {
        st.interrupt();
    }

}