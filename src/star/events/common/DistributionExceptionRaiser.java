package star.events.common;

import star.event.Event;
import star.event.EventController;
import star.event.Raiser;

public interface DistributionExceptionRaiser extends Raiser
{
	Event getEvent();

	EventController getTarget();

	Throwable getException();
}
