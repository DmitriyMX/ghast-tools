package ghast.database;

import ghast.XLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class JdbcTemplate implements JdbcOperations {

	private DataSource dataSource;

	public JdbcTemplate(DataSource dataSource) {
		setDataSource(dataSource);
	}

	@Override
	public void execute(String sql) throws DataAccessException {
		XLog.debug("Execute SQL: {0}", sql);

		Connection connection = openConnection();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			throw new DataAccessException("Error execute SQL", sql, e);
		} finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}

	@Override
	public <T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException {
		XLog.debug("Execute SQL: {0}", sql);

		Connection connection = openConnection();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			return rse.extractData(resultSet);
		} catch (SQLException e) {
			throw new DataAccessException("Error execute SQL", sql, e);
		} finally {
			closeResultSet(resultSet);
			closeStatement(statement);
			closeConnection(connection);
		}
	}

	@Override
	public <T> List<T> query(String sql, final RowMapper<T> rowMapper) throws DataAccessException {
		return query(sql, rs -> {
			List<T> resultList = new ArrayList<>();
			int rowNum = 0;
			while (rs.next()) {
				resultList.add(rowMapper.mapRow(rs, rowNum++));
			}
			return resultList;
		});
	}

	@Override
	public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return query(sql, rs -> {
			if (rs.next()) {
				T resultObj = rowMapper.mapRow(rs, 0);

				if (rs.next()) {
					throw new IncorrectResultSizeDataAccessException(1);
				}

				return resultObj;
			} else {
				throw new EmptyResultDataAccessException(1);
			}
		});
	}

	@Override
	public Map<String, Object> queryForMap(String sql) throws DataAccessException {
		return queryForObject(sql, (rs, rowNum) -> {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			Map<String, Object> resultMap = new LinkedHashMap<>(columnCount);

			for (int i = 1; i <= columnCount; i++) {
				String key = lookupColumnName(metaData, i);
				Object value = getResultSetRawValue(rs, i);
				resultMap.put(key, value);
			}

			return resultMap;
		});
	}

	@Override
	public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {
		return query(sql, (rs, rowNum) -> {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			Map<String, Object> resultMap = new LinkedHashMap<>(columnCount);

			for (int i = 1; i <= columnCount; i++) {
				String key = lookupColumnName(metaData, i);
				Object value = getResultSetRawValue(rs, i);
				resultMap.put(key, value);
			}

			return resultMap;
		});
	}

	@Override
	public int update(String sql) throws DataAccessException {
		XLog.debug("Execute SQL: {0}", sql);

		Connection connection = openConnection();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			int rows = statement.executeUpdate(sql);
			XLog.debug("Affected {0} rows", rows);
			return rows;
		} catch (SQLException e) {
			XLog.error("Error execute SQL: {0}", e.getMessage(), e);
			return 0;
		} finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}

	@Override
	public int delete(String sql) throws DataAccessException {
		return update(sql);
	}

	private Connection openConnection() {
		try {
			return getDataSource().getConnection();
		}
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", ex);
		}
	}

	private String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.isEmpty()) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}

	private Object getResultSetRawValue(ResultSet resultSet, int index) throws SQLException {
		Object obj = resultSet.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}

		if (obj instanceof Blob) {
			Blob blob = (Blob) obj;
			obj = blob.getBytes(1, (int) blob.length());
		} else if (obj instanceof Clob) {
			Clob clob = (Clob) obj;
			obj = clob.getSubString(1, (int) clob.length());
		} else if ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className)) {
			obj = resultSet.getTimestamp(index);
		} else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = resultSet.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = resultSet.getTimestamp(index);
			}
			else {
				obj = resultSet.getDate(index);
			}
		} else if (obj instanceof Date) {
			if ("java.sql.Timestamp".equals(resultSet.getMetaData().getColumnClassName(index))) {
				obj = resultSet.getTimestamp(index);
			}
		}

		return obj;
	}

	private void closeResultSet(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				XLog.debug("Could not close JDBC ResultSet", e);
			} catch (Exception e) {
				XLog.debug("Unexpected exception on closing JDBC ResultSet", e);
			}
		}
	}

	private void closeStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				XLog.debug("Could not close JDBC Statement", e);
			} catch (Exception e) {
				XLog.debug("Unexpected exception on closing JDBC Statement", e);
			}
		}
	}

	private void closeConnection(Connection con) {
		if (con == null) {
			return;
		}

		try {
			con.close();
		} catch (SQLException e) {
			XLog.debug("Could not close JDBC Connection", e);
		} catch (Exception e) {
			XLog.debug("Unexpected exception on closing JDBC Connection", e);
		}
	}
}
