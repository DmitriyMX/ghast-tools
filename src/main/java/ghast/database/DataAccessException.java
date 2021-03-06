package ghast.database;

import lombok.Getter;

@Getter
public class DataAccessException extends RuntimeException {

    private String sql;

    public DataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DataAccessException(String msg, String sql, Throwable cause) {
        this(msg, cause);
        this.sql = sql;
    }
}
