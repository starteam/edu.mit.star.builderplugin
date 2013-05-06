package star.event;

import java.util.HashSet;
import java.util.Iterator;

public class Adapter
{

	boolean debug = false;

	public Adapter(EventController owner, boolean debug)
	{
		setOwner(owner);
		setParent(null);
		this.debug = debug;
	}

	public Adapter(EventController owner)
	{
		setOwner(owner);
		setParent(null);
	}

	private EventController owner = null;

	public EventController getOwner()
	{
		return this.owner;
	}

	private void setOwner(EventController owner)
	{
		if( debug )
			System.err.println("setOwner " + this + " " + owner);
		this.owner = owner;
	}

	private Object parent = null;

	public Object getParent()
	{
		return this.parent;
	}

	public void setParent(Object parent)
	{
		if( debug )
			System.err.println("setParent " + this + " " + parent);
		try
		{
			if( (parent instanceof EventController) || (parent instanceof java.awt.Component) )
			{
				this.parent = parent;
			}
			else if( null != parent )
			{
				throw new Exception("A non-null parent must either implement EventController or be a subclass of java.awt.Component");
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	java.util.HashSet components = null;

	public boolean hasComponent(Object c)
	{
		try
		{
			if( null != c )
			{
				return components.contains(c);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void addComponent(Object c)
	{
		if( debug )
			System.err.println("addComponent " + this + " " + c);
		try
		{
			Event.invalidate();
			if( null == components )
			{
				components = new java.util.HashSet();
			}
			if( c != null )
			{
				if( (c instanceof EventController) || (c instanceof java.awt.Component) )
				{
					components.add(c);
					if( c instanceof EventController )
					{
						((EventController) c).getAdapter().setParent(getOwner());
						((EventController) c).addNotify();
					}
				}
				else
				{
					throw new Exception("A component must either implement EventController or be a subclass of java.awt.Component");
				}
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	public void removeComponent(Object c)
	{
		if( debug )
			System.err.println("removeComponent " + this + " " + c);
		try
		{
			Event.invalidate();
			if( null != c && null != components )
			{
				if( (c instanceof EventController) || (c instanceof java.awt.Component) )
				{
					components.remove(c);
					if( c instanceof EventController )
					{
						((EventController) c).removeNotify();
						((EventController) c).getAdapter().setParent(null);
					}
				}
				else
				{
					System.err.println("ERROR: attempted to remove object: " + c);
					System.err.println("  object must either implement EventController or be a subclass of java.awt.Component");
				}
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	public java.util.Iterator getComponents()
	{
		if( components == null )
		{
			return (new java.util.HashSet()).iterator();
		}
		else
		{
			return components.iterator();
		}
	}

	protected java.util.Vector contained = null;

	public boolean isContained(Class eventClass)
	{
		try
		{
			if( null != this.contained )
			{
				return(this.contained.contains(eventClass));
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void addContained(Class eventClass)
	{
		if( debug )
			System.err.println("addContained " + this + " " + eventClass);
		try
		{
			Event.invalidate();
			if( null == this.contained )
			{
				this.contained = new java.util.Vector();
			}
			if( !isContained(eventClass) )
			{
				this.contained.addElement(eventClass);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	public void removeContained(Class eventClass)
	{
		if( debug )
			System.err.println("removeContained " + this + " " + eventClass);
		try
		{
			Event.invalidate();
			if( isContained(eventClass) )
			{
				this.contained.removeElement(eventClass);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	protected java.util.Vector excludeExternal = null;

	public boolean isExcludeExternal(Class eventClass)
	{
		try
		{
			if( null != this.excludeExternal )
			{
				return(this.excludeExternal.contains(eventClass));
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void addExcludeExternal(Class eventClass)
	{
		try
		{
			Event.invalidate();
			if( null == this.excludeExternal )
			{
				this.excludeExternal = new java.util.Vector();
			}
			if( !isExcludeExternal(eventClass) )
			{
				this.excludeExternal.addElement(eventClass);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	public void removeExcludeExternal(Class eventClass)
	{
		try
		{
			Event.invalidate();
			if( isExcludeExternal(eventClass) )
			{
				this.excludeExternal.removeElement(eventClass);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	protected java.util.Vector excludeInternal = null;

	public boolean isExcludeInternal(Class eventClass)
	{
		try
		{
			if( null != this.excludeInternal )
			{
				return(this.excludeInternal.contains(eventClass));
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void addExcludeInternal(Class eventClass)
	{
		try
		{
			Event.invalidate();
			if( null == this.excludeInternal )
			{
				this.excludeInternal = new java.util.Vector();
			}
			if( !isExcludeInternal(eventClass) )
			{
				this.excludeInternal.addElement(eventClass);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	public void removeExcludeInternal(Class eventClass)
	{
		try
		{
			Event.invalidate();
			if( isExcludeInternal(eventClass) )
			{
				this.excludeInternal.removeElement(eventClass);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	// java.util.Vector handled = null;
	java.util.HashSet<Class> handled = null;

	public boolean isHandled(Class eventClass)
	{
		try
		{
			if( null != handled )
			{
				return handled.contains(eventClass);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void addHandled(Class eventClass)
	{
		if( debug )
			System.err.println("addHandled " + this + " " + eventClass);
		try
		{
			Event.invalidate();
			if( !isHandled(eventClass) )
			{
				if( null == handled )
				{
					// handled = new java.util.Vector();
					handled = new java.util.HashSet<Class>();
				}
				if( getOwner() instanceof Listener )
				{
					// handled.addElement(eventClass);
					handled.add(eventClass);
				}
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	public void removeHandled(Class eventClass)
	{
		if( debug )
			System.err.println("removeHandled " + this + " " + eventClass);
		try
		{
			Event.invalidate();
			if( isHandled(eventClass) )
			{
				// handled.removeElement(eventClass);
				handled.remove(eventClass);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	public boolean isControlled(Class eventClass)
	{
		if( isHandled(eventClass) )
		{
			return true;
		}
		if( isExcludeInternal(eventClass) )
		{
			return true;
		}
		if( isExcludeExternal(eventClass) )
		{
			return true;
		}
		if( isContained(eventClass) )
		{
			return true;
		}
		if( isGateHandled(eventClass) )
		{
			return true;
		}
		return false;
	}

	protected Object eventId = null;

	public Object getEventId()
	{
		return this.eventId;
	}

	public void setEventId(Object eventId)
	{
		this.eventId = eventId;
	}

	transient protected java.util.Hashtable listeners = null;

	public java.util.Hashtable getListeners()
	{
		return this.listeners;
	}

	public void setListeners(java.util.Hashtable listeners)
	{
		this.listeners = listeners;
	}

	boolean valid = true;

	public boolean isValid()
	{
		return valid;
	}

	public void invalidate()
	{
		valid = false;
	}

	public void validate()
	{
		boolean valid = true;
		Iterator<HashSet<Gate>> iter = gatedAnd.values().iterator();
		while( iter.hasNext() )
		{
			Iterator<Gate> iter2 = iter.next().iterator();
			while( iter2.hasNext() )
			{
				Gate gate = iter2.next();
				if( !gate.isValid() )
				{
					gate.validate();
				}
				valid &= gate.isValid();
			}
		}
		this.valid = valid;
	}

	public void eventRaised(Event event)
	{
		EventController owner = getOwner();
		if( owner instanceof Listener && isHandled(event.getClass()) )
		{
			((Listener) owner).eventRaised(event);
		}
		if( owner instanceof GatedListener )
		{
			if( gatedAnd.containsKey(event.getClass()) )
			{
				Iterator<Gate> iter = gatedAnd.get(event.getClass()).iterator();
				while( iter.hasNext() )
				{
					Gate next = iter.next();
					next.process(event);
				}
			}
		}
	}

	java.util.Hashtable<Class, HashSet<Gate>> gatedAnd = new java.util.Hashtable<Class, HashSet<Gate>>();

	GatedListener getGatedListener()
	{
		GatedListener ret = null;
		EventController owner = getOwner();
		if( owner instanceof GatedListener )
		{
			ret = (GatedListener) owner;
		}
		else
		{
			new RuntimeException("Owner has to implement GatedListener!");
		}
		return ret;
	}

	Listener getListener()
	{
		Listener ret = null;
		EventController owner = getOwner();
		if( owner instanceof Listener )
		{
			ret = (Listener) owner;
		}
		else
		{
			new RuntimeException("Owner has to implement GatedListener!");
		}
		return ret;
	}

	public Gate addGatedAnd(Class[] eventsHandled, Class[] eventRaised, boolean interrupt)
	{
		Gate ret = null;
		if( eventsHandled != null && eventsHandled.length != 0 )
		{
			Gate gate = new AndGate(eventsHandled, eventRaised, this, interrupt);
			for (int i = 0; i < eventsHandled.length; i++)
			{
				if( !gatedAnd.contains(eventsHandled[i]) )
				{
					gatedAnd.put(eventsHandled[i], new java.util.HashSet<Gate>());
				}
				gatedAnd.get(eventsHandled[i]).add(gate);
			}
			ret = gate;
		}
		return ret;
	}

	public void removeGatedAnd(Class[] eventsHandled, Class[] eventRaised)
	{
		if( eventsHandled != null && eventsHandled.length != 0 )
		{
			for (int i = 0; i < eventsHandled.length; i++)
			{
				if( gatedAnd.contains(eventsHandled[i]) )
				{
					Iterator<Gate> iter = gatedAnd.get(eventsHandled[i]).iterator();
					while( iter.hasNext() )
					{
						Gate next = iter.next();
						if( next.equals(eventsHandled, eventRaised) )
						{
							iter.remove();
						}
					}
					if( gatedAnd.get(eventsHandled[i]).size() == 0 )
					{
						gatedAnd.remove(eventsHandled[i]);
					}
				}
			}
		}
	}

	public boolean isGateHandled(Class event)
	{
		return gatedAnd.containsKey(event);
	}
}