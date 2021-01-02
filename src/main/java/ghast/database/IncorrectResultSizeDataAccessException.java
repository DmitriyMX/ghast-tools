package ghast.database;

public class IncorrectResultSizeDataAccessException extends DataAccessException {

	public IncorrectResultSizeDataAccessException(int expectedSize) {
		super("Incorrect result size: expected " + expectedSize);
	}
}
