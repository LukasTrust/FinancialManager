package financialmanager.Utils.Result;

/**
 * Represents a successful result.
 */
public record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public boolean isErr() {
        return false;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public E getError() {
        throw new UnsupportedOperationException("Cannot get error from Ok<T, E>");
    }
}