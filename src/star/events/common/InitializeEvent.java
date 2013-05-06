package star.events.common;

import star.event.Event;

public class InitializeEvent extends Event
{
	private static final long serialVersionUID = 1L;

	public InitializeEvent(InitializeEvent event)
	{
		super(event);
	}

	public InitializeEvent(InitializeRaiser source)
	{
		super(source);
	}

	public InitializeEvent(InitializeRaiser source, boolean valid)
	{
		super(source, valid);
	}
}
