package cz.muni.exceptions.listener.db.mybatis.handlers;

import cz.muni.exceptions.listener.db.model.TicketClass;
import junit.framework.Assert;
import org.apache.ibatis.type.JdbcType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Jan Ferko
 */
@RunWith(MockitoJUnitRunner.class)
public class TicketClassHandlerTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private CallableStatement callableStatement;

    private TicketClassHandler handler;

    @Before
    public void setUp() {
        this.handler = new TicketClassHandler();
    }

    @Test
    public void testSetNonNullParameter() throws SQLException {
        handler.setNonNullParameter(preparedStatement, 1, TicketClass.DATABASE, JdbcType.INTEGER);
        Mockito.verify(preparedStatement).setInt(1, TicketClass.DATABASE.getId());
    }

    @Test
    public void testGetUnknownResultFromResultSetAndIndex() throws SQLException {
        Mockito.when(resultSet.getInt(1)).thenReturn(-1);

        TicketClass actual = handler.getNullableResult(resultSet, 1);
        Assert.assertEquals(TicketClass.UNKNOWN, actual);

        Mockito.verify(resultSet).getInt(1);
    }

    @Test
    public void testGetResultFromResultSetAndIndex() throws SQLException {
        Mockito.when(resultSet.getInt(1)).thenReturn(1);

        TicketClass expected = TicketClass.find(1);
        TicketClass actual = handler.getNullableResult(resultSet, 1);
        Assert.assertEquals(expected, actual);

        Mockito.verify(resultSet).getInt(1);
    }

    @Test
    public void testGetResultFromResultSetAndColumnName() throws SQLException {
        Mockito.when(resultSet.getInt("ticketClass")).thenReturn(1);

        TicketClass expected = TicketClass.find(1);
        TicketClass actual = handler.getNullableResult(resultSet, "ticketClass");
        Assert.assertEquals(expected, actual);

        Mockito.verify(resultSet).getInt("ticketClass");
    }

    @Test
    public void testGetUnknownResultFromResultSetAndColumnName() throws SQLException {
        Mockito.when(resultSet.getInt("unknownClass")).thenReturn(-1);

        TicketClass actual = handler.getNullableResult(resultSet, "unknownClass");
        Assert.assertEquals(TicketClass.UNKNOWN, actual);

        Mockito.verify(resultSet).getInt("unknownClass");
    }

    @Test
    public void testGetResultFromCallableStatement() throws SQLException {
        Mockito.when(callableStatement.getInt(1)).thenReturn(1);

        TicketClass expected = TicketClass.find(1);
        TicketClass actual = handler.getNullableResult(callableStatement, 1);
        Assert.assertEquals(expected, actual);

        Mockito.verify(callableStatement).getInt(1);
    }

    @Test
    public void testGetUnknownResultFromCallableStatement() throws SQLException {
        Mockito.when(callableStatement.getInt(1)).thenReturn(-1);

        TicketClass actual = handler.getNullableResult(callableStatement, 1);
        Assert.assertEquals(TicketClass.UNKNOWN, actual);

        Mockito.verify(callableStatement).getInt(1);
    }
}
