package star.event;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;

import star.events.common.DistributionExceptionEvent;
import star.events.common.DistributionExceptionRaiser;

public class Event extends java.util.EventObject
{
	private static final long serialVersionUID = 1L;

	private static java.util.Random random = new java.util.Random(System.currentTimeMillis());

	private static java.util.Vector validRaisers = null;

	private Event nestedEvent = null;

	private long timestamp = 0;

	private boolean valid = true;

	private static Frame lock = new Frame();

	public long getTimestamp()
	{
		return timestamp;
	}

	public Event getNestedEvent()
	{
		return nestedEvent;
	}

	public static void invalidate()
	{
		synchronized (lock.getTreeLock())
		{
			validRaisers = null;
		}
	}

	public Event(Event event)
	{
		super(event.getSource());
		nestedEvent = event;
		this.valid = event.valid;
	}

	public Event(star.event.Raiser source)
	{
		super(source);
	}

	public Event(star.event.Raiser source, boolean valid)
	{
		super(source);
		this.valid = valid;
	}

	public boolean isValid()
	{
		return valid;
	}

	static int depth = 0;

	final static String myStr = "                                                                               ";

	public void raise()
	{
		raiseImpl();
	}

	protected void raiseImpl()
	{
		// boolean log = hasListeners() && this.isValid() ;
		boolean log = false;
		if (log)
		{
			depth++;
			System.err.println(myStr.substring(0, depth) + "  in  " + this.isValid() + " " + this.getClass().getSimpleName() + " by " + this.getSource().getClass().getSimpleName() + " " + this.getSource().hashCode());
		}
		try
		{
			synchronized (lock.getTreeLock())
			{
				timestamp = System.currentTimeMillis();
				boolean distribute = hasListeners();
				if (distribute)
				{
					distributeEvent();
				}
			}
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
		}
		if (!this.getClass().equals(Event.class))
		{
			try
			{
				((Event) (this.getClass().getSuperclass().getConstructor(new Class[] { this.getClass().getSuperclass() })).newInstance(new Object[] { this })).raise();
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
		if (log)
		{
			System.err.println(myStr.substring(0, depth) + "  out " + this.isValid() + " " + this.getClass().getSimpleName() + " by " + this.getSource().getClass().getSimpleName());
			depth--;
			// (new RuntimeException("Trace")).printStackTrace();
		}
	}

	private boolean hasListeners()
	{
		validateListeners();
		if (getSource() instanceof star.event.Raiser)
		{
			star.event.Adapter r = ((star.event.Raiser) getSource()).getAdapter();
			java.util.Hashtable listeners = r.getListeners();
			java.util.Vector eventListeners = null;
			if (listeners.containsKey(this.getClass()))
			{
				eventListeners = (java.util.Vector) listeners.get(this.getClass());
				if (null != eventListeners)
				{
					return !eventListeners.isEmpty();
				}
			}
			else
			{
				boolean ret = false;
				Object container = findContainer();
				boolean hasListeners = findListeners(container);
				if (hasListeners)
				{
					listeners = r.getListeners();
					if (listeners.containsKey(this.getClass()))
					{
						eventListeners = (java.util.Vector) listeners.get(this.getClass());
						if (null != eventListeners)
						{
							ret = !eventListeners.isEmpty();
						}
					}
				}
				return ret;
			}
		}
		return false;
	}

	protected void throwDistributionException(final Event event, final EventController controller, final Throwable throwable)
	{
		if (!(event instanceof DistributionExceptionEvent))
		{
			try
			{
				(new DistributionExceptionEvent(new DistributionExceptionRaiser()
				{

					public Event getEvent()
					{
						return event;
					}

					public Throwable getException()
					{
						return throwable;
					}

					public EventController getTarget()
					{
						return controller;
					}

					public void addNotify()
					{
					}

					public Adapter getAdapter()
					{
						return ((Raiser) event.getSource()).getAdapter();
					}

					public void removeNotify()
					{
					}
				})).raise();
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void distributeEvent()
	{
		star.event.Adapter r = ((star.event.Raiser) getSource()).getAdapter();
		java.util.Hashtable listeners = r.getListeners();
		if (listeners.containsKey(this.getClass()))
		{
			java.util.Vector eventListeners = (java.util.Vector) listeners.get(this.getClass());
			if (null != eventListeners)
			{
				ArrayList components = new ArrayList();
				java.util.Enumeration enumeration = eventListeners.elements();
				while (enumeration.hasMoreElements())
				{
					final EventController listener = (EventController) enumeration.nextElement();
					try
					{
						listener.getAdapter().eventRaised(this);
						if (!listener.getAdapter().isValid())
						{
							components.add(listener);
						}
					}
					catch (final Throwable ex)
					{
						ex.printStackTrace();
						throwDistributionException(this, listener, ex);
					}
				}
				Iterator iter = components.iterator();
				while (iter.hasNext())
				{
					EventController listener = (EventController) iter.next();
					try
					{
						listener.getAdapter().validate();
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
						throwDistributionException(this, listener, ex);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void validateListeners()
	{
		if (null == validRaisers)
		{
			validRaisers = new java.util.Vector();
		}
		star.event.Adapter r = ((star.event.Raiser) getSource()).getAdapter();
		if (null != r)
		{
			if ((null == r.getEventId()) || !validRaisers.contains(r.getEventId()))
			{
				r.setEventId(new Double(random.nextDouble()));
				validRaisers.addElement(r.getEventId());
				r.setListeners(null);
			}
			if (null == r.getListeners())
			{
				r.setListeners(new java.util.Hashtable());
			}
		}
	}

	private Object findContainer()
	{
		Object container = getSource();

		while (container != null)
		{
			if (container instanceof star.event.EventController)
			{
				star.event.Adapter adapter = ((star.event.EventController) container).getAdapter();
				if (null != adapter)
				{
					if (adapter.isContained(this.getClass()))
					{
						return container;
					}
					if (adapter.getParent() != null)
					{
						container = adapter.getParent();
						continue;
					}
					if( container instanceof javax.swing.JPopupMenu )
					{
						javax.swing.JPopupMenu popup = (javax.swing.JPopupMenu)container;
						if (null == ((java.awt.Component) container).getParent())
						{
							if( null == popup.getInvoker() )
							{
								return container ;
							}
							else
							{
								container = popup.getInvoker();
								continue;
								
							}
						}
						else
						{
							container = ((java.awt.Component) container).getParent();
							continue;
						}						
					}
					if (container instanceof java.awt.Component)
					{
						if (null != ((java.awt.Component) container).getParent())
						{
							container = ((java.awt.Component) container).getParent();
							continue;
						}
					}
					if (container instanceof java.awt.MenuComponent)
					{
						if (null != ((java.awt.MenuComponent) container).getParent())
						{
							container = ((java.awt.MenuComponent) container).getParent();
							continue;
						}

					}
					return container;
				}
				System.err.println("Error: " + this.getSource() + "has a null adapter.");
				return null;
			}
			else if( container instanceof javax.swing.JPopupMenu )
			{
				javax.swing.JPopupMenu popup = (javax.swing.JPopupMenu)container;
				if (null == ((java.awt.Component) container).getParent())
				{
					if( null == popup.getInvoker() )
					{
						return container ;
					}
					else
					{
						container = popup.getInvoker();
						continue;
						
					}
				}
				else
				{
					container = ((java.awt.Component) container).getParent();
					continue;
				}
				
			}
			else if (container instanceof java.awt.Component)
			{
				if (null == ((java.awt.Component) container).getParent())
				{
					return container;
				}
				container = ((java.awt.Component) container).getParent();
				continue;
			}
			else if (container instanceof java.awt.MenuComponent)
			{
				if (null != ((java.awt.MenuComponent) container).getParent())
				{
					container = ((java.awt.MenuComponent) container).getParent();
					continue;
				}
			}
			System.err.println("Error: " + this.getSource() + "is neither an EventController nor a java.awt.Component.");
			return null;
		}
		return null;
	}

	private boolean findListeners(Object container)
	{
		boolean foundListeners = false;
		if (container instanceof star.event.EventController)
		{
			foundListeners = findListener((star.event.EventController) container);
		}
		if (container instanceof java.awt.Container)
		{
			boolean foundCL = findContainerListeners((java.awt.Container) container);
			foundListeners = foundListeners || foundCL;
		}
		if (container instanceof java.awt.MenuComponent)
		{
			boolean foundCL = findMenuContainerListeners((java.awt.MenuComponent) container);
			foundListeners = foundListeners || foundCL;
		}
		return foundListeners;
	}

	@SuppressWarnings("unchecked")
	private boolean findListener(star.event.EventController controller)
	{
		star.event.Adapter adapter = controller.getAdapter();
		if (adapter.isControlled(this.getClass()))
		{
			if (null == adapter.getListeners())
			{
				adapter.setListeners(new java.util.Hashtable());
			}
			java.util.Vector listeners = (java.util.Vector) ((EventController) getSource()).getAdapter().getListeners().get(this.getClass());
			if (null == listeners)
			{
				listeners = new java.util.Vector();
				((EventController) getSource()).getAdapter().getListeners().put(this.getClass(), listeners);
			}

			if (null != listeners)
			{
				if (!listeners.contains(controller))
				{
					if ((!isExcludeExternal(controller)) && !isAncestorOfEventRaiser(controller))
					{
						if (controller instanceof Listener || controller instanceof GatedListener)
						{
							listeners.addElement(controller);
						}
						findAdapterContainedListeners(controller);
						return true;
					}
					if ((!isExcludeInternal(controller)) && isAncestorOfEventRaiser(controller))
					{
						if (controller instanceof Listener || controller instanceof GatedListener)
						{
							listeners.addElement(controller);
						}
						findAdapterContainedListeners(controller);
						return true;
					}
				}
			}
		}
		else
		{
			return findAdapterContainedListeners(controller);
		}
		return false;
	}

	private boolean isAncestorOfEventRaiser(Object container)
	{
		if (this.getSource().equals(container))
		{
			return true;
		}
		if ((this.getSource() instanceof java.awt.Component) && (container instanceof java.awt.Container))
		{
			return ((java.awt.Container) container).isAncestorOf((java.awt.Component) this.getSource());
		}
		return false;
	}

	private boolean findMenuContainerListeners(final java.awt.MenuComponent container)
	{
		boolean foundContainerListeners = false;
		if (container instanceof java.awt.MenuBar)
		{
			if (((java.awt.MenuBar) container).getMenuCount() != 0)
			{
				for (int i = 0; ((java.awt.MenuBar) container).getMenuCount() != i; i++)
				{
					boolean foundCL = findListeners((Object) ((java.awt.MenuBar) container).getMenu(i));
					foundContainerListeners = foundContainerListeners || foundCL;
				}
			}
		}
		if (container instanceof java.awt.Menu)
		{
			if (((java.awt.Menu) container).getItemCount() != 0)
			{
				for (int i = 0; ((java.awt.Menu) container).getItemCount() != i; i++)
				{
					boolean foundCL = findListeners((Object) ((java.awt.Menu) container).getItem(i));
					foundContainerListeners = foundContainerListeners || foundCL;
				}
			}
		}
		return foundContainerListeners;
	}

	private boolean findContainerListeners(final java.awt.Container container)
	{
		boolean foundContainerListeners = false;
		if (!isExcludeExternal(container))
		{
			if (container.getComponentCount() != 0)
			{
				java.awt.Component[] comps = ((java.awt.Container) container).getComponents();
				for (int i = 0; comps.length != i; i++)
				{
					boolean foundCL = findListeners((Object) comps[i]);
					foundContainerListeners = foundContainerListeners || foundCL;
				}
			}
		}
		return foundContainerListeners;
	}

	private boolean findAdapterContainedListeners(EventController container)
	{
		boolean foundContainerListeners = false;
		java.util.Iterator iter = container.getAdapter().getComponents();
		while (iter.hasNext())
		{
			boolean foundCL = findListeners(iter.next());
			foundContainerListeners = foundContainerListeners || foundCL;
		}
		return foundContainerListeners;
	}

	private boolean isExcludeExternal(Object obj)
	{
		if (obj instanceof star.event.EventController)
		{
			star.event.Adapter adapter = ((star.event.EventController) obj).getAdapter();
			if (null != adapter)
			{
				boolean ret = adapter.isExcludeExternal(this.getClass());
				return ret;
			}
		}
		return false;
	}

	private boolean isExcludeInternal(Object obj)
	{
		if (obj instanceof star.event.EventController)
		{
			star.event.Adapter adapter = ((star.event.EventController) obj).getAdapter();
			if (null != adapter)
			{
				return adapter.isExcludeInternal(this.getClass());
			}
		}
		return false;
	}

	public String toString()
	{
		return getClass().getName() + "[source=" + getSource() + ",timestamp=" + getTimestamp() + "]";
	}
}
