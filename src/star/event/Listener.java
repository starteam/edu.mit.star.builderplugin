package star.event;

public interface Listener extends EventController
{
	void eventRaised(Event event);
}