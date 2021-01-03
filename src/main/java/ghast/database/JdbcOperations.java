package ghast.database;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JdbcOperations {

	void execute(String sql) throws DataAccessException;

	<T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException;

	<T> Optional<T> queryOne(String sql, ResultSetExtractor<T> rse) throws DataAccessException;

	<T> List<T> queryList(String sql, RowMapper<T> rowMapper) throws DataAccessException;

	Map<String, Object> queryForMap(String sql) throws DataAccessException;

	List<Map<String, Object>> queryForMapList(String sql) throws DataAccessException;

	int update(String sql) throws DataAccessException;
}
