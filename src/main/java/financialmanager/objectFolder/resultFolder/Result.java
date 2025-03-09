package financialmanager.objectFolder.resultFolder;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A sealed interface representing the result of an operation,
 * which can either be an Ok (successful result) or an Err (error result).
 */
public sealed interface Result<T, E> permits Ok, Err {
    boolean isOk();
    boolean isErr();
    T getValue();
    E getError();

    /**
     * Maps the value of an Ok result using the provided function, or returns the Err unchanged.
     */
    default <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
        if (isOk()) {
            return new Ok<>(mapper.apply(getValue()));
        }
        return new Err<>(getError());
    }

    /**
     * Maps the error of an Err result using the provided function, or returns the Ok unchanged.
     */
    default <F> Result<T, F> mapError(Function<? super E, ? extends F> mapper) {
        if (isErr()) {
            return new Err<>(mapper.apply(getError()));
        }
        return new Ok<>(getValue());
    }

    /**
     * Applies a function that returns another Result if this is Ok, otherwise returns Err unchanged.
     */
    default <U> Result<U, E> flatMap(Function<? super T, ? extends Result<U, E>> mapper) {
        if (isOk()) {
            return mapper.apply(getValue());
        }
        return new Err<>(getError());
    }

    default T orElseGet(Supplier<? extends T> supplier) {
        return isOk() ? getValue() : supplier.get();
    }
}