package ghast.database;

public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

	public EmptyResultDataAccessException(int expectedSize) {
		super(expectedSize);
	}
}
