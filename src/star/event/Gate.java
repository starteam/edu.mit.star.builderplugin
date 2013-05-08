package star.event;

public interface Gate
{
	public void process(Event event);

	public boolean isValid();

	public void validate();

	public boolean equals(Class[] eventsHandled, Class[] eventsRaised);
}
