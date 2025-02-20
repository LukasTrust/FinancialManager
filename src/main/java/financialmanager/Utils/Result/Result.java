package financialmanager.Utils.Result;

public sealed interface Result<T, E> permits Ok, Err  {
    boolean isOk();
    boolean isErr();

    T getValue();

    E getError(); // Returns error for Err
}