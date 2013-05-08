package star.event;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class AndGate implements Gate
{
	Class[] eventsHandled;
	Class[] eventsRaised;
	Event[] eventsHandledData;
	boolean valid = false;
	boolean interrupt = false;
	Adapter adapter;
	static Object lock = new Object();
	boolean debug;

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[AndGate,valid=");
		sb.append(valid);
		sb.append(",interrupt=");
		sb.append(interrupt);
		sb.append(",events=");
		sb.append(Arrays.toString(eventsHandledData));
		sb.append("]");
		return sb.toString();
	}

	public void setDebug(boolean debug)
	{
		if( !debug )
		{
			System.err.println("Disabling debug " + toString());
		}
		this.debug = debug;
		if( debug )
		{
			System.err.println("Enabling debug " + toString());
		}
	}

	public AndGate(Class[] eventsHandled, Class[] eventsRaised, Adapter adapter, boolean interrupt)
	{
		this.eventsHandled = eventsHandled;
		this.eventsRaised = eventsRaised;
		this.eventsHandledData = new Event[this.eventsHandled.length];
		this.adapter = adapter;
		this.interrupt = interrupt;
	}

	public void process(Event event)
	{
		if( event != null )
		{
			synchronized( lock )
			{
				Class clazz = event.getClass();
				for (int i = 0; i < eventsHandled.length; i++)
				{
					if( clazz.equals(eventsHandled[i]) )
					{
						invalidate();
						eventsHandledData[i] = event.isValid() ? event : null;
						break;
					}
				}
			}
			if( debug )
			{
				(new RuntimeException(toString())).printStackTrace();
			}
		}
	}

	public boolean isValid()
	{
		synchronized( lock )
		{
			return valid;
		}
	}

	@SuppressWarnings("unchecked")
	public void invalidate()
	{
		synchronized( lock )
		{
			valid = false;
			adapter.invalidate();
			if( interrupt )
			{
				boolean raise = true;
				for (int i = 0; (i < eventsHandled.length) && raise; i++)
				{
					raise &= (eventsHandledData[i] != null);
				}
				if( raise )
				{
					getAdapter().getGatedListener().eventsRaised(eventsHandledData, valid);
				}
			}
			if( eventsRaised != null && eventsRaised.length != 0 )
			{
				for (int i = 0; i < eventsRaised.length; i++)
				{
					try
					{
						Constructor<Event> c = eventsRaised[i].getConstructor(new Class[] { Raiser.class, boolean.class });
						Event e = c.newInstance(new Object[] { getAdapter().getOwner(), Boolean.FALSE });
						e.raise();
					}
					catch( IllegalArgumentException e )
					{
						e.printStackTrace();
					}
					catch( SecurityException e )
					{
						e.printStackTrace();
					}
					catch( InstantiationException e )
					{
						e.printStackTrace();
					}
					catch( IllegalAccessException e )
					{
						e.printStackTrace();
					}
					catch( InvocationTargetException e )
					{
						e.printStackTrace();
					}
					catch( NoSuchMethodException e )
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void validate()
	{
		synchronized( lock )
		{
			boolean raise = true;
			for (int i = 0; (i < eventsHandled.length) && raise; i++)
			{
				raise &= (eventsHandledData[i] != null);
			}
			if( raise )
			{

				valid = true;
				getAdapter().getGatedListener().eventsRaised(eventsHandledData, valid);
			}
		}
	}

	private Adapter getAdapter()
	{
		return adapter;
	}

	public boolean equals(Class[] eventsHandled, Class[] eventsRaised)
	{
		return Arrays.equals(eventsHandled, this.eventsHandled) && Arrays.equals(eventsRaised, this.eventsRaised);
	}

}
