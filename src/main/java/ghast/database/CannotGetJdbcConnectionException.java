package ghast.database;

import java.sql.SQLException;

public class CannotGetJdbcConnectionException extends DataAccessException {

	public CannotGetJdbcConnectionException(String msg, SQLException ex) {
		super(msg, ex);
	}
}
