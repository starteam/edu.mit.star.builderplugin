package star.observable;

public abstract class ObservableValueTransformer<T,Q> implements ObservableValue<T> 
{
	private ObservableValue<Q> source;

	public ObservableValueTransformer(ObservableValue<Q> source)
	{
		this.source = source;
	}

	@Override
    public void addObservableListener(ObservableListener l)
    {
		source.addObservableListener(l);
    }

	@Override
    public void removeObservableListener(ObservableListener l)
    {
		source.removeObservableListener(l);
    }

    public ObservableValue<Q> getSource()
    {
	    return source;
    }
	
}
