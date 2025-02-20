package financialmanager.Utils.Result;

import java.util.function.Consumer;
import java.util.function.Function;

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
     * Executes the given consumer if the result is Ok.
     */
    default void ifOk(Consumer<? super T> consumer) {
        if (isOk()) {
            consumer.accept(getValue());
        }
    }

    /**
     * Executes the given consumer if the result is Err.
     */
    default void ifErr(Consumer<? super E> consumer) {
        if (isErr()) {
            consumer.accept(getError());
        }
    }
}