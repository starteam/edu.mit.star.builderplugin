package star.observable;

public interface ObservableValue<T>
{
	void addObservableListener( ObservableListener l ) ;
	void removeObservableListener( ObservableListener l ) ;
	T get();
}
