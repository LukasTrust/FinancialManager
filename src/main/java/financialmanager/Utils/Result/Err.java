package financialmanager.Utils.Result;

/**
 * Represents an error result.
 */
public record Err<T, E>(E error) implements Result<T, E> {
    @Override
    public boolean isOk() {
        return false;
    }

    @Override
    public boolean isErr() {
        return true;
    }

    @Override
    public T getValue() {
        throw new UnsupportedOperationException("Err does not contain a value");
    }

    @Override
    public E getError() {
        return error;
    }
}