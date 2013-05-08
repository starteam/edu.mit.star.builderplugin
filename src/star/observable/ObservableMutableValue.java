package star.observable;

public interface ObservableMutableValue<T> extends ObservableValue<T>
{
	void set(T it);
}
