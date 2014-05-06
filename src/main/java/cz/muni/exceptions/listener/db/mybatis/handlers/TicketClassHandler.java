package cz.muni.exceptions.listener.db.mybatis.handlers;

import cz.muni.exceptions.listener.db.model.TicketClass;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mybatis handler for {@link cz.muni.exceptions.listener.db.model.TicketClass} enum.
 *
 * @author Jan Ferko
 */
public class TicketClassHandler extends BaseTypeHandler<TicketClass> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TicketClass parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getId());
    }

    @Override
    public TicketClass getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int id = rs.getInt(columnName);
        return TicketClass.find(id);
    }

    @Override
    public TicketClass getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int id = rs.getInt(columnIndex);
        return TicketClass.find(id);
    }

    @Override
    public TicketClass getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int id = cs.getInt(columnIndex);
        return TicketClass.find(id);
    }
}
