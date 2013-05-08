package star.event;

public interface GatedListener extends EventController
{
	void eventsRaised(Event[] event, boolean valid);
}
