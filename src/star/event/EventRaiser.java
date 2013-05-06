package star.event;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;


public class EventRaiser
{
	public void raiseDataEvent( final Class<? extends star.event.Raiser> raiser , final EventController controller , final HashMap<String, Object> data ) throws Throwable
	{
		String event = getEvent( raiser.getCanonicalName() ) ;
		InvocationHandler handler = new InvocationHandler() {

			Adapter adapter = null ;
			public Adapter getAdapter( EventController c)
			{
				if( adapter == null )
				{
					adapter = new Adapter( c );
					adapter.setParent( controller );
				}
				return adapter ;
			}
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
				if( "getAdapter".equals( method.getName()) )
				{
					return getAdapter( (EventController) proxy );
				}
				else if( "equals".equals( method.getName() ) )
				{
					if( Proxy.isProxyClass( args[0].getClass() ) )
					{
						return true ;
					}
					else
					{
						return false ;
					}
				}
				else if( "toString".equals( method.getName() ) ) 
				{
					return "[Proxy " + raiser + " " + controller + "]";
				}
	            return data != null ? data.get(method.getName()) : null ;
            }};        
		Object proxy = Proxy.newProxyInstance( raiser.getClassLoader(), new Class[] { raiser } , handler ) ;
		Constructor constructor = Class.forName(event).getConstructor( new Class[] { raiser } );
		Event eventObject = (Event) constructor.newInstance( new Object[] { proxy } ) ;
		eventObject.raise();
	}

	public void raiseDataLessEvent( final Class<? extends star.event.Raiser> raiser , final EventController controller ) throws Throwable
	{
		raiseDataEvent(raiser, controller, null);
	}
	
	String getRaiser(String handledClass)
	{
		if( handledClass.indexOf("Raiser") != -1 )
		{
			return handledClass;
		}
		if( handledClass.indexOf("Event") == -1 )
		{
			return handledClass + "Raiser";
		}
		return handledClass.substring(0, handledClass.lastIndexOf("Event")) + "Raiser";
	}

	String getEvent(String handledClass)
	{
		if( handledClass.indexOf("Event") != -1 )
		{
			return handledClass;
		}
		if( handledClass.indexOf("Raiser") == -1 )
		{
			return handledClass + "Event";
		}
		return handledClass.substring(0, handledClass.lastIndexOf("Raiser")) + "Event";
	}

	
}
