package edu.mit.star.builderplugin.codegen;

import static edu.mit.star.builderplugin.codegen.Utilities.addMarker;
import static edu.mit.star.builderplugin.codegen.Utilities.createDerivedResource;
import static edu.mit.star.builderplugin.codegen.Utilities.getAnnotation;
import static edu.mit.star.builderplugin.codegen.Utilities.getEvent;
import static edu.mit.star.builderplugin.codegen.Utilities.hasAnnotation;
import static edu.mit.star.builderplugin.codegen.Utilities.isImplementingInterface;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import star.annotations.GeneratedClass;
import star.annotations.Handles;
import star.annotations.Preferences;
import star.annotations.Properties;
import star.annotations.Wrap;
import star.event.Adapter;
import star.event.Event;
import star.event.EventController;
import star.event.Raiser;

public class SignalComponent
{
	public static final String MARKER_TYPE = SignalComponent.class.getName();
	public static final String EXTENDS_VIOLATION = "extend";

	enum AnnotationProperties
	{
		extend, handles, raises, contains, excludeInternal, excludeExternal, // SignalComponent
		prefix, loadResource, application // Preferences
		;
	};

	boolean profile = true;
	long TIME = 1000L * 1000L * 500L; // 500ms
	long STEP = 1000L * 1000L; // 1ms

	/**
	 * Entry function It call code generation and it checks architectural errors
	 * 
	 * @param sourceFile
	 * @param sourceNode
	 */
	public void generate(IFile sourceFile, TypeDeclaration sourceNode)
	{
		try
		{
			sourceFile.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
			if (!sourceNode.isInterface())
			{
				ITypeBinding bindings = sourceNode.resolveBinding();
				if (Utilities.hasAnnotation(bindings, star.annotations.SignalComponent.class))
				{
					generateCode(sourceFile, sourceNode, bindings);
					checkSourceRequirements(sourceFile, sourceNode, bindings);
				}
			}
		}
		catch (CoreException ex)
		{
			ex.printStackTrace();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	private void checkSourceRequirements(IFile sourceFile, TypeDeclaration sourceNode, ITypeBinding bindings)
	{
		if (!bindings.getSuperclass().getQualifiedName().equals(bindings.getQualifiedName() + "_generated"))
		{
			addMarker(sourceFile, bindings.getQualifiedName() + " must extend " + (bindings.getQualifiedName() + "_generated"), sourceNode.getStartPosition(), sourceNode.getLength(), IMarker.SEVERITY_ERROR, MARKER_TYPE, EXTENDS_VIOLATION, bindings.getName());
		}
	}

	private void generateCode(IFile sourceFile, TypeDeclaration sourceNode, ITypeBinding bindings) throws CoreException
	{
		IPackageBinding pack = bindings.getTypeDeclaration().getPackage();
		GeneratedClass sourceCode = generateSourceCode(pack, bindings);
		InputStream source = new ByteArrayInputStream(sourceCode.getSource().getBytes());
		createDerivedResource(getIFile(pack, bindings.getTypeDeclaration(), sourceFile), source, sourceFile, MARKER_TYPE);
	}

	private GeneratedClass generateSourceCode(IPackageBinding pack, ITypeBinding thisclass)
	{
		String className = thisclass.getName() + "_generated";
		ITypeBinding superclass = (ITypeBinding) getAnnotationPropertyBinding(thisclass, AnnotationProperties.extend.name(), star.annotations.SignalComponent.class);
		GeneratedClass generatedClass = new star.annotations.GeneratedClass(pack.getName() + "." + className, GeneratedClass.ABSTRACT);

		generatedClass.setParent(superclass.getQualifiedName());
		generatedClass.addMember(GeneratedClass.PRIVATE | GeneratedClass.STATIC | GeneratedClass.FINAL, "long", "serialVersionUID", "1L");

		generateConstructorCode(className, generatedClass, superclass);
		generateEventListenerCode(className, generatedClass, superclass, thisclass);
		generateListener(thisclass, superclass, generatedClass);

		generateProperties(thisclass, superclass, generatedClass);

		generatePreferences(thisclass, superclass, generatedClass);

		generateWrappers(thisclass,superclass,generatedClass);
		
		return generatedClass;
	}

	private static String capitalize(String s)
	{
		if (s.length() == 0)
			return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	private void generateWrappers(ITypeBinding thisclass, ITypeBinding superclass, GeneratedClass generatedClass)
	{
		for (IMethodBinding m : thisclass.getDeclaredMethods())
		{
			if (Utilities.hasAnnotation(m, Wrap.class))
			{
				ITypeBinding[] params = m.getParameterTypes();
				if (params.length == 0)
				{
					Class<? extends Object> wrap_interface = Runnable.class ;
					String method = "run";
					StringBuffer sb = new StringBuffer();
					sb.append( "new " + wrap_interface.getName() + "()\n\t{" ) ;
					sb.append( "\n\tpublic void " + method + "() {" ) ;
					sb.append( "\n\t\t" + m.getName() + "();" ) ;
					sb.append( "\n\t}" ) ;
					sb.append( "\n}" ) ;
					String body = "return " + sb.toString() + ";";
					generatedClass.addMethod(GeneratedClass.DEFAULT | GeneratedClass.ABSTRACT, m.getReturnType().getQualifiedName(), m.getName(), new String[0], new String[0], null);
					Object ret = getAnnotationPropertyBinding(m,"type",Wrap.class);
					if( ret.toString().contains( Wrap.Types.SwingUtilitiesInvokeLater.toString() ))
					{
						body = "javax.swing.SwingUtilities.invokeLater("+sb.toString()+");";
						generatedClass.addMethod(GeneratedClass.DEFAULT , "void" , m.getName() + "_" + Wrap.Types.SwingUtilitiesInvokeLater.toString() , new String[0] , new String[0] , body ) ; 
					}
					else
					{
						generatedClass.addMethod(GeneratedClass.DEFAULT , wrap_interface.getName() , m.getName() + "_" + wrap_interface.getSimpleName() , new String[0] , new String[0] , body ) ;	
					}

				}
			}
		}
	}
	
	private void generatePreferences(ITypeBinding thisclass, ITypeBinding superclass, GeneratedClass generatedClass)
	{
		if (hasAnnotation(thisclass, Preferences.class))
		{
			String initializePreferencesMemberWith = null;
			Object data = getAnnotationPropertyBinding(thisclass, AnnotationProperties.prefix.name(), Preferences.class);
			String className = thisclass.getQualifiedName();
			String prefix = String.valueOf(data);
			if (data == null || prefix.equals(Preferences.DEFAULT))
			{
				prefix = className;
			}
			StringBuffer sb = new StringBuffer();
			sb.append("if( preferences == null )");
			sb.append("\n{");
			sb.append("\n\ttry");
			sb.append("\n\t{");
			sb.append("\n\t\tplugin.preferences.Preferences pref = (plugin.preferences.Preferences) plugin.Loader.getDefaultLoader().getPlugin(plugin.preferences.Preferences.class.getName(), plugin.preferences.PreferencesImplementation.class.getName());");
			sb.append("\n\t\tthis.preferences = pref.getPreferences(\"" + prefix + "\");");
			sb.append("\n\t}");
			sb.append("\n\tcatch( plugin.PluginException ex )");
			sb.append("\n\t{");
			sb.append("\n\t\tex.printStackTrace();\n");
			sb.append("\n\t}\n");
			sb.append("}\n");
			sb.append("if( preferences == null )");
			sb.append("\n{");
			sb.append("\n\treturn java.util.prefs.Preferences.userRoot().node(\"" + prefix + "\");");
			sb.append("\n}\n");
			sb.append("return preferences;");
			generatedClass.addMethod(GeneratedClass.DEFAULT, java.util.prefs.Preferences.class.getName(), "getPreferences", null, null, sb.toString());
			sb = new StringBuffer();
			sb.append("try");
			sb.append("\n{");
			sb.append("\n\tplugin.preferences.Preferences pref = (plugin.preferences.Preferences) plugin.Loader.getDefaultLoader().getPlugin(plugin.preferences.Preferences.class.getName(), plugin.preferences.PreferencesImplementation.class.getName());");
			sb.append("\n\tthis.preferences = pref.getPreferences(name);");
			sb.append("\n}");
			sb.append("\ncatch( plugin.PluginException ex )");
			sb.append("\n{");
			sb.append("\n\tex.printStackTrace();\n");
			sb.append("\n}\n");
			sb.append("if( preferences == null )");
			sb.append("\n{");
			sb.append("\n\treturn java.util.prefs.Preferences.userRoot().node(name);");
			sb.append("\n}\n");
			sb.append("return preferences;");
			generatedClass.addMethod(GeneratedClass.DEFAULT, java.util.prefs.Preferences.class.getName(), "getPreferences", new String[] { String.class.getName() }, new String[] { "name" }, sb.toString());

			Object resource_obj = getAnnotationPropertyBinding(thisclass, AnnotationProperties.loadResource.name(), Preferences.class);
			Object application_obj = getAnnotationPropertyBinding(thisclass, AnnotationProperties.application.name(), Preferences.class);
			if (resource_obj != null && application_obj != null)
			{
				String resource = String.valueOf(resource_obj);
				String application = String.valueOf(application_obj);
				if (!"".equals(resource) && !"".equals(application))
				{
					sb = new StringBuffer();
					sb.append("\ntry");
					sb.append("\n{");
					sb.append("\n\tplugin.preferences.Preferences pref = (plugin.preferences.Preferences) plugin.Loader.getDefaultLoader().getPlugin(plugin.preferences.Preferences.class.getName(), plugin.preferences.PreferencesImplementation.class.getName());");
					sb.append("\n\tjava.util.Properties prop = new java.util.Properties();");
					sb.append("\n\tprop.load(this.getClass().getClassLoader().getResourceAsStream(\"" + resource + "\"));");
					sb.append("\n\tpref.setApplication(\"" + application + "\",prop);");
					sb.append("}\n");
					sb.append("\ncatch( Throwable ex )");
					sb.append("\n{");
					sb.append("\n\tex.printStackTrace();\n");
					sb.append("\n}");
					sb.append("\nreturn getPreferences();");
					generatedClass.addMethod(GeneratedClass.DEFAULT, java.util.prefs.Preferences.class.getName(), "loadPreferences", null, null, sb.toString());
					initializePreferencesMemberWith = "loadPreferences()";
				}
			}
			generatedClass.addMember(GeneratedClass.PRIVATE, java.util.prefs.Preferences.class.getName(), "preferences", initializePreferencesMemberWith);

		}
	}

	private void generateProperties(ITypeBinding thisclass, ITypeBinding superclass, GeneratedClass generatedClass)
	{
		if (hasAnnotation(thisclass, Properties.class))
		{
			Boolean propertyChangeSupport = (Boolean)getAnnotationPropertyBinding(thisclass, "propertyChangeListener", Properties.class);
			boolean pcs =propertyChangeSupport != null && propertyChangeSupport.booleanValue(); 
			if( pcs )
			{
				generatedClass.addMember( GeneratedClass.PRIVATE , "java.beans.PropertyChangeSupport", "propertyChangeSupport", MessageFormat.format("new {0}(this)", "java.beans.PropertyChangeSupport"));
				generatedClass.addMethod( GeneratedClass.PUBLIC, "void", "addPropertyChangeListener", new String[] { "java.beans.PropertyChangeListener" }, new String[] { "listener" } , "propertyChangeSupport.addPropertyChangeListener( listener );" );
				generatedClass.addMethod( GeneratedClass.PUBLIC, "void", "removePropertyChangeListener", new String[] { "java.beans.PropertyChangeListener" }, new String[] { "listener" } , "propertyChangeSupport.removePropertyChangeListener( listener );" );
				
			}
			Object data = getAnnotationPropertyBinding(thisclass, "value", Properties.class);
			if (data != null)
			{
				Object[] values;
				if (data instanceof Object[])
				{
					values = (Object[]) data;
				}
				else
				{
					values = new Object[1];
					values[0] = data;
				}
				for (Object v : values)
				{
					if (v instanceof IAnnotationBinding)
					{
						Property p = new Property((IAnnotationBinding) v);
						String name = p.name();
						ITypeBinding type = p.type();
						String value = p.value();
						String params = p.params();

						if (value.equals("new"))
						{
							value = "new " + type + params + "()";
						}
						generatedClass.addMember(GeneratedClass.PRIVATE, type.getQualifiedName() + params, name, value.equals("") ? null : value);
						
						if (p.getter() != star.annotations.Property.NOT_GENERATED)
						{
							String body = "return this." + name + " ;";
							generatedClass.addMethod(p.getter() == star.annotations.Property.PROTECTED ? GeneratedClass.PROTECTED : GeneratedClass.PUBLIC, type.getQualifiedName() + params, "get" + capitalize(name), null, null, body);
							if ((type.getName() + params).equals("boolean"))
							{
								generatedClass.addMethod(p.getter() == star.annotations.Property.PROTECTED ? GeneratedClass.PROTECTED : GeneratedClass.PUBLIC, type.getQualifiedName() + params, "is" + capitalize(name), null, null, body);
							}

						}

						if (p.getter() != star.annotations.Property.NOT_GENERATED && type.isArray())
						{
							String type2 = type.getQualifiedName().substring(0, type.getQualifiedName().length() - 2);
							String body = "return this." + name + "[i] ;";
							generatedClass.addMethod(p.getter() == star.annotations.Property.PROTECTED ? GeneratedClass.PROTECTED : GeneratedClass.PUBLIC, type2 + params, "get" + capitalize(name), new String[] { "int" }, new String[] { "i" }, body);
							if ((type + params).equals("boolean"))
							{
								generatedClass.addMethod(p.getter() == star.annotations.Property.PROTECTED ? GeneratedClass.PROTECTED : GeneratedClass.PUBLIC, type2 + params, "is" + capitalize(name), new String[] { "int" }, new String[] { "i" }, body);
							}

						}

						if (p.setter() != star.annotations.Property.NOT_GENERATED)
						{
							String body ;
							if( pcs )
							{
								StringBuffer b = new StringBuffer();
								b.append( MessageFormat.format( "{1} old_{0} = this.{0};\n" , name , type.getQualifiedName() + params ) ) ;
								b.append( MessageFormat.format( "this.{0} = {0};\n" , name ) );
								b.append( MessageFormat.format( "propertyChangeSupport.firePropertyChange(\"{0}\" , old_{0} , {0});" , name )) ;
								body = b.toString();
							}
							else
							{
								body = "this." + name + " = " + name + " ;";
							}
							generatedClass.addMethod(p.setter() == star.annotations.Property.PROTECTED ? GeneratedClass.PROTECTED : GeneratedClass.PUBLIC, "void", "set" + capitalize(name), new String[] { type.getQualifiedName() + params }, new String[] { name }, body);
						}
					}

				}
			}

		}
	}

	class Property
	{
		int getter;
		int setter;
		ITypeBinding type;
		String name;
		String value;
		String params;

		public int getter()
		{
			return getter;
		}

		public int setter()
		{
			return setter;
		}

		public ITypeBinding type()
		{
			return type;
		}

		public String name()
		{
			return name;
		}

		public String value()
		{
			return value;
		}

		public String params()
		{
			return params;
		}

		public Property(IAnnotationBinding binding)
		{
			for (IMemberValuePairBinding pair : binding.getAllMemberValuePairs())
			{
				String key = pair.getName();
				Object value = pair.getValue();
				if (key.equals("name"))
				{
					this.name = value.toString();
				}
				if (key.equals("value"))
				{
					this.value = value.toString();
				}
				if (key.equals("getter"))
				{
					this.getter = ((Integer) value).intValue();
				}
				if (key.equals("setter"))
				{
					this.setter = ((Integer) value).intValue();
				}
				if (key.equals("type"))
				{
					this.type = (ITypeBinding) value;
				}
				if (key.equals("params"))
				{
					this.params = value.toString();
				}
			}
		}

		@Override
		public String toString()
		{
			return MessageFormat.format("[name={0} type={1} value={2} getter={3} setter={4} params={5}]", name, type, value, getter, setter, params);
		}
	}

	private void generateConstructorCode(String className, GeneratedClass generatedClass, ITypeBinding superclass)
	{
		try
		{
			IMethodBinding[] methods = superclass.getDeclaredMethods();
			for (IMethodBinding m : methods)
			{
				if (m.isConstructor())
				{
					ArrayList<String> types = new ArrayList<String>();
					ArrayList<String> args = new ArrayList<String>();
					ArrayList<String> throwables = new ArrayList<String>();
					StringBuffer body = new StringBuffer(" ");
					int count = 0;
					for (ITypeBinding param : m.getParameterTypes())
					{
						types.add(param.getQualifiedName());
						String name = param.getName();
						if (name.contains("["))
						{
							name = name.substring(0, name.indexOf("["));
						}
						if (name.contains("<"))
						{
							name = name.substring(0, name.indexOf("<"));
						}

						boolean suffix;
						if (!Character.isLowerCase(name.charAt(0)))
						{
							name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
							suffix = false;
						}
						else
						{
							suffix = true;
						}

						if (args.contains(name) || suffix)
						{
							name = name + count++;
						}
						args.add(name);
						body.append(name + ",");
					}

					for (ITypeBinding param : m.getExceptionTypes())
					{
						throwables.add(param.getQualifiedName());
					}

					body.setLength(body.length() - 1);

					generatedClass.addMethod(GeneratedClass.PUBLIC, "", className, types.toArray(new String[0]), args.toArray(new String[0]), throwables.toArray(new String[0]), "super(" + body.toString().trim() + ");");
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	private void generateEventListenerCode(String className, GeneratedClass generatedClass, ITypeBinding superclass, ITypeBinding thisclass)
	{
		if (!hasMethod(superclass, "getAdapter"))
		{
			generatedClass.addMember(GeneratedClass.PRIVATE, Adapter.class.getName(), "adapter", null);
			generatedClass.addMethod(GeneratedClass.PUBLIC, Adapter.class.getName(), "getAdapter", null, null, "if( adapter == null )\n{\n\tadapter = new " + Adapter.class.getName() + "(this);\n}\nreturn adapter;");
		}

		boolean hasAddNotify = hasMethod(superclass, "addNotify");
		generatedClass.addMethod(GeneratedClass.PUBLIC, "void", "addNotify", null, null, getAddNotifyBody(superclass, hasAddNotify, thisclass));

		boolean hasRemoveNotify = hasMethod(superclass, "removeNotify");
		generatedClass.addMethod(GeneratedClass.PUBLIC, "void", "removeNotify", null, null, getRemoveNotifyBody(superclass, hasRemoveNotify, thisclass));

		generatedClass.addInterface(EventController.class.getName());
	}

	private String getRemoveNotifyBody(ITypeBinding superclass, boolean hasSuper, ITypeBinding thisclass)
	{
		StringBuilder body = new StringBuilder();
		if (hasSuper)
		{
			body.append("super.removeNotify();\n");
		}
		body.append(updateAdapterCall(thisclass, AnnotationProperties.handles.name(), "removeHandled"));
		body.append(updateAdapterCall(thisclass, AnnotationProperties.contains.name(), "removeContained"));
		body.append(updateAdapterCall(thisclass, AnnotationProperties.excludeExternal.name(), "removeExcludeExternal"));
		body.append(updateAdapterCall(thisclass, AnnotationProperties.excludeInternal.name(), "removeExcludeInternal"));
		body.append(generateRemoveNotifyHandles(thisclass));
		return body.toString();
	}

	private String getAddNotifyBody(ITypeBinding superclass, boolean hasSuper, ITypeBinding thisclass)
	{
		StringBuilder body = new StringBuilder();
		if (hasSuper)
		{
			body.append("super.addNotify();\n");
		}
		body.append(updateAdapterCall(thisclass, AnnotationProperties.handles.name(), "addHandled"));
		body.append(updateAdapterCall(thisclass, AnnotationProperties.contains.name(), "addContained"));
		body.append(updateAdapterCall(thisclass, AnnotationProperties.excludeExternal.name(), "addExcludeExternal"));
		body.append(updateAdapterCall(thisclass, AnnotationProperties.excludeInternal.name(), "addExcludeInternal"));
		body.append(generateAddNotifyHandles(thisclass));
		return body.toString();
	}

	private String generateAddNotifyHandles(ITypeBinding thisclass)
	{
		StringBuilder body = new StringBuilder();
		for (IMethodBinding m : thisclass.getDeclaredMethods())
		{
			if (Utilities.hasAnnotation(m, Handles.class))
			{

				ITypeBinding[] params = m.getParameterTypes();
				for (ITypeBinding b : params)
				{
					body.append("getAdapter().addHandled( " + getEvent(b.getQualifiedName()) + ".class );\n");
				}
			}
		}
		return body.toString();
	}

	private String generateRemoveNotifyHandles(ITypeBinding thisclass)
	{
		StringBuilder body = new StringBuilder();
		for (IMethodBinding m : thisclass.getDeclaredMethods())
		{
			if (Utilities.hasAnnotation(m, Handles.class))
			{

				ITypeBinding[] params = m.getParameterTypes();
				for (ITypeBinding b : params)
				{
					body.append("getAdapter().removeHandled( " + getEvent(b.getQualifiedName()) + ".class );\n");
				}
			}
		}
		return body.toString();
	}

	private String updateAdapterCall(ITypeBinding thisclass, String property, String method)
	{
		StringBuffer ret = new StringBuffer();
		Iterator<String> iterator = getClasses(thisclass, property, star.annotations.SignalComponent.class).iterator();
		while (iterator.hasNext())
		{
			String next = iterator.next().toString();
			ret.append("getAdapter()." + method + "(" + Utilities.getEvent(next) + ".class );\n");
		}
		return ret.toString();
	}

	private void generateListener(ITypeBinding thisclass, ITypeBinding superclass, star.annotations.GeneratedClass generatedClass)
	{
		StringBuffer eventRaiserBody = new StringBuffer();
		StringBuffer eventsRaiserBody = new StringBuffer();
		if (hasMethod(superclass, "eventRaised"))
		{
			eventRaiserBody.append("super.eventRaised( event );\n");
		}

		if (generateHandleAnnotationCode(thisclass, superclass, generatedClass))
		{
			eventRaiserBody.append("eventRaisedHandles(event);\n");
		}
		// if (generateAndGateHandlesListener(thisclass, superclass, generatedClass))
		// {
		// eventsRaiserBody.append("eventAndGateRaisedHandles(event,valid);");
		// }

		if (eventRaiserBody.length() != 0)
		{
			generatedClass.addInterface(star.event.Listener.class.getName());
			generatedClass.addMethod(GeneratedClass.PUBLIC, "void", "eventRaised", new String[] { "final " + Event.class.getName() }, new String[] { "event" }, eventRaiserBody.toString());
		}
		if (eventsRaiserBody.length() != 0)
		{
			generatedClass.addInterface(star.event.GatedListener.class.getName());
			generatedClass.addMethod(GeneratedClass.PUBLIC, "void", "eventsRaised", new String[] { "final " + Event.class.getName(), "final boolean" }, new String[] { "event[]", "valid" }, eventsRaiserBody.toString());
		}

		for (String raisedClass : getClasses(thisclass, AnnotationProperties.raises.name(), star.annotations.SignalComponent.class))
		{
			addRaiser(generatedClass, raisedClass);
		}
	}

	private boolean generateHandleAnnotationCode(ITypeBinding declaration, ITypeBinding extendsName, star.annotations.GeneratedClass generatedClass)
	{
		boolean ret = false;
		StringBuffer body = new StringBuffer();
		for (IMethodBinding m : declaration.getDeclaredMethods())
		{
			if (Utilities.hasAnnotation(m, Handles.class))
			{

				ITypeBinding[] params = m.getParameterTypes();
				if (params.length == 1 && isImplementingInterface(params[0], Raiser.class))
				{
					StringBuffer prefixCode = new StringBuffer();
					ret = true;
					for (String raiser : getClasses(m, AnnotationProperties.raises.name(), Handles.class))
					{
						prefixCode.append("(new " + Utilities.getEvent(raiser) + "(this,false)).raise();\n");
						addRaiser(generatedClass, Utilities.getRaiser(raiser));
					}

					if (getBooleanAnnotationPropertyBinding(m, "handleValid", Handles.class, true)) // handle valid only
					{
						body.append("if( event.getClass().getName().equals( \"" + Utilities.getEvent(params[0].getQualifiedName()) + "\" ) && event.isValid() ) " //
						        + "\n{" //
						        + (profile ? "\n\t long start = System.nanoTime();" : "") //
						        + "\n\t" + prefixCode + "\n\t" + m.getName() + "( (" + params[0].getQualifiedName() + ")event.getSource());" //
						        + (profile ? "\n\t long end = System.nanoTime();\n\t if( end - start > " + TIME + " ) " //
						                + "{ System.out.println( this.getClass().getName() + \"." + m.getName() + " \"  + ( end-start )/" + STEP + " ); } " : "") //
						        + "\n}\n");
					}
					else
					{
						body.append("if( event.getClass().getName().equals( \"" + Utilities.getEvent(params[0].getQualifiedName()) + "\" ) && !event.isValid() ) " //
						        + "\n{" //
						        + (profile ? "\n\t long start = System.nanoTime();" : "") //
						        + "\n" + prefixCode + "\n\t" + m.getName() + "( (" + params[0].getQualifiedName() + ")event.getSource());" //
						        + (profile ? "\n\t long end = System.nanoTime();\n\t if( end - start > " + TIME + " ) " //
						                + "{ System.out.println( this.getClass().getName() + \"." + m.getName() + " \"  + ( end-start )/" + STEP + " ); } " : "") //
						        + "\n}\n");
					}

					generatedClass.addMethod(GeneratedClass.DEFAULT | GeneratedClass.ABSTRACT, m.getReturnType().getQualifiedName(), m.getName(), new String[] { params[0].getQualifiedName() }, new String[] { params[0].getName() }, null);
				}
				else
				{
					addMarker(m.getJavaElement(), m.getName() + " must has exactly one argument that is Raiser.", IMarker.SEVERITY_WARNING, MARKER_TYPE);
				}
			}
		}
		if (ret)
		{
			generatedClass.addMethod(GeneratedClass.PRIVATE, "void", "eventRaisedHandles", new String[] { "final " + Event.class.getName() }, new String[] { "event" }, body.toString());
		}
		return ret;
	}

	private String getClasslessClassNameOnly(String name)
	{
		String handledClass = name;
		if (handledClass.indexOf(".class") != -1)
		{
			handledClass = handledClass.substring(0, handledClass.indexOf(".class"));
		}
		handledClass = handledClass.substring(handledClass.lastIndexOf('.') + 1);
		return handledClass.trim();
	}

	private void addRaiser(star.annotations.GeneratedClass generatedClass, String raisedClass)
	{
		generatedClass.addInterface(raisedClass);
		if (!generatedClass.hasMethod("raise_" + getClasslessClassNameOnly(Utilities.getEvent(raisedClass))))
		{
			generatedClass.addMethod(GeneratedClass.PUBLIC, "void", "raise_" + getClasslessClassNameOnly(Utilities.getEvent(raisedClass)), null, null, "(new " + Utilities.getEvent(raisedClass) + "(this)).raise();");
		}
	}

	private Collection<String> getClasses(IBinding thisclass, String property, Class<? extends Object> annotationClass)
	{
		ArrayList<String> elements = new ArrayList<String>();
		Object obj = getAnnotationPropertyBinding(thisclass, property, annotationClass);
		if (obj instanceof Object[])
		{
			for (Object element : (Object[]) obj)
			{
				if (element instanceof ITypeBinding)
				{
					elements.add(((ITypeBinding) element).getQualifiedName());
				}
			}
		}
		return elements;
	}

	Object getAnnotationPropertyBinding(IBinding thisclass, String property, Class<? extends Object> annotationClass)
	{
		IAnnotationBinding annotation = getAnnotation(thisclass, annotationClass);
		if (annotation != null)
		{
			IMemberValuePairBinding[] bindings = annotation.getAllMemberValuePairs();
			for (IMemberValuePairBinding binding : bindings)
			{
				if (property.equals(binding.getName()))
				{
					return binding.getValue();
				}
			}
		}
		return null;
	}

	boolean getBooleanAnnotationPropertyBinding(IBinding thisclass, String property, Class<? extends Object> annotationClass, boolean defaultValue)
	{
		Object value = getAnnotationPropertyBinding(thisclass, property, annotationClass);
		boolean ret = defaultValue;
		if (value != null && value instanceof Boolean && ((Boolean) value).booleanValue() == false)
		{
			ret = false;
		}
		return ret;
	}

	private boolean hasMethod(ITypeBinding type, String methodName)
	{
		boolean ret = false;
		for (IMethodBinding method : type.getDeclaredMethods())
		{
			if (methodName.equals(method.getName()))
			{
				ret = true;
				break;
			}
		}
		if (!ret)
		{
			ITypeBinding superclass = type.getSuperclass();
			if (superclass != null)
			{
				ret = hasMethod(superclass, methodName);
			}
		}
		return ret;
	}

	private IFile getIFile(IPackageBinding pack, ITypeBinding type, IFile sourceFile)
	{
		return sourceFile.getProject().getFile("/generated_src/" + Utilities.join(pack.getNameComponents(), "/") + type.getName() + "_generated.java");
	}

}
