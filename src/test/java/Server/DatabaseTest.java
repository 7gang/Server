package Server;

import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseTest {

    private String convertToString(ArrayList<String> input) {
        String output = "";
        for (String line : input) output += line + ",,";
        return output;
    }

    @Test
    public void testConstructor() throws SQLException, ClassNotFoundException {

        Database db = new Database();

        assertNotNull(db);
        assertNotEquals(db.toString(), "");

        String data = convertToString(db.getData());
        System.out.println("Data:" + data);

    }

    @Test
    public void testAddQuote() throws ClassNotFoundException, SQLException {

        Database db = new Database();
        String previous = convertToString(db.getData()); // in case previous test data persists
        
        db.addQuote("best quote ever");
        assertEquals(convertToString(db.getData()), previous + "best quote ever,,");

        db.addQuote("another great quote");
        assertEquals(convertToString(db.getData()), previous + "best quote ever,,another great quote,,");

    }

    @Test
    public void testDeleteQuote() throws ClassNotFoundException, SQLException {

        Database db = new Database();
        db.addQuote("a great quote");
        db.addQuote("a not-that-great quote");

        db.deleteQuote("a not-that-great quote");
        assertEquals(convertToString(db.getData()), "a great quote,,");

    }

    @Test(expected = SQLException.class)
    public void testDeleteQuoteException() throws ClassNotFoundException, SQLException {

        Database db = new Database();
        db.addQuote("a great quote");
        
        db.deleteQuote("non-existing quote");
    
    }

    @Test
    public void testEditQuote() throws ClassNotFoundException, SQLException {

        Database db = new Database();
        String previous = convertToString(db.getData()); // in case previous test data persists

        db.addQuote("a quote with a typo");
        db.editQuote("a quote with a typo", "a quote");
        assertEquals(convertToString(db.getData()), previous + "a quote,,");

    }

    @Test(expected = SQLException.class)
    public void testEditQuoteException() throws ClassNotFoundException, SQLException {

        Database db = new Database();

        db.addQuote("another quote with a typo");
        db.editQuote("non-existing quote", "another fixed quote");
        
    }

}
