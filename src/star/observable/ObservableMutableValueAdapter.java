package star.observable;

import java.util.HashSet;

public abstract class ObservableMutableValueAdapter<T> implements ObservableMutableValue<T>
{
	private transient HashSet<ObservableListener> listeners = new HashSet<ObservableListener>();
	
	@Override
    public void addObservableListener(ObservableListener l)
    {
		listeners.add(l);
    }

	@Override
    public void removeObservableListener(ObservableListener l)
    {
		listeners.remove(l);
    }

}
