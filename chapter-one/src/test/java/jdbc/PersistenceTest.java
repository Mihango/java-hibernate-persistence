package jdbc;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class PersistenceTest {
    Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:./db1", "sa", "");
    }

    @BeforeClass
    public void setup() {
        final String DROP = "DROP TABLE messages IF EXISTS;";
        final String CREATE = "CREATE TABLE messages (" +
                "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "text VARCHAR(256) NOT NULL)";

        try(Connection connection = getConnection()) {
            // clear out old data
            try(PreparedStatement ps = connection.prepareStatement(DROP)) {
                ps.execute();
            }
            // create table
            try(PreparedStatement ps = connection.prepareStatement(CREATE)) {
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public MessageEntity saveMessage(String text) {
        final String INSERT = "INSERT INTO messages(text) VALUES (?)";
        MessageEntity message = null;
        try(Connection connection = getConnection()) {
            try(PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, text);
                ps.execute();

                try(ResultSet keys = ps.getGeneratedKeys()) {
                    if(!keys.next()) throw new SQLException("No generated keys");
                    message = new MessageEntity(keys.getLong(1), text);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return message;
    }

    @Test
    public void readMessage() {
        final String text = "Hello, World!";
        MessageEntity message = saveMessage(text);

        final String SELECT = "SELECT id, text FROM messages";
        List<MessageEntity> list = new ArrayList<>();

        try(Connection connection = getConnection()) {
            try(PreparedStatement ps = connection.prepareStatement(SELECT)) {
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        System.out.println("Testing double result set reads: " + rs.getLong(1));
                        MessageEntity newMsg = new MessageEntity(rs.getLong(1), rs.getString(2));
                        list.add(newMsg);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        for(MessageEntity m : list) {
            System.out.println(m);
        }
        assertEquals(list.get(0), message);
    }
}