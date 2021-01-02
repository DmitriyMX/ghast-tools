package ghast.database;

import java.util.List;
import java.util.Map;

public interface JdbcOperations {

	void execute(String sql) throws DataAccessException;

	<T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException;

	<T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException;

	<T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException;

	Map<String, Object> queryForMap(String sql) throws DataAccessException;

	List<Map<String, Object>> queryForList(String sql) throws DataAccessException;

	int update(String sql) throws DataAccessException;

	int delete(String sql) throws DataAccessException;
}
