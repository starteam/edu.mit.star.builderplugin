package star.events.common;

import star.event.Event;

public class DistributionExceptionEvent extends Event
{
	private static final long serialVersionUID = 1L;

	public DistributionExceptionEvent(DistributionExceptionRaiser source, boolean valid)
	{
		super(source, valid);
	}

	public DistributionExceptionEvent(DistributionExceptionRaiser source)
	{
		super(source);
	}

	public DistributionExceptionEvent(Event event)
	{
		super(event);
	}
}
