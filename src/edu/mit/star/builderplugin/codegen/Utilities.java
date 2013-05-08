package edu.mit.star.builderplugin.codegen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.text.edits.ReplaceEdit;

public class Utilities
{
	public static String VIOLATION = "VIOLATION" ;
	public static String BINDING = "BINDING" ;
	public static boolean hasAnnotation(IBinding bindings, Class annotation)
	{
		boolean ret = false;
		IAnnotationBinding[] annotations = bindings.getAnnotations();
		for (IAnnotationBinding a : annotations)
		{
			if (annotation.getName().equals(a.getAnnotationType().getQualifiedName()))
			{
				ret = true;
			}
		}
		return ret;
	}
	
	public static boolean isImplementingInterface( ITypeBinding binding, Class<? extends Object> superinterface )
	{
		ITypeBinding[] interfaces = binding.getInterfaces();
		boolean hasSuperInterface = false;
		for (ITypeBinding ifc : interfaces)
		{
			if (superinterface.getName().equals(ifc.getQualifiedName()))
			{
				hasSuperInterface = true;
				break;
			}
		}
		if (!hasSuperInterface)
		{
			for (ITypeBinding ifc : interfaces)
			{

				if (isImplementingInterface(ifc,superinterface))
				{
					hasSuperInterface = true;
					break;
				}
			}
		}
		return hasSuperInterface;
	}

	public static IAnnotationBinding getAnnotation(IBinding binding, Class annotation)
	{
		IAnnotationBinding[] annotations = binding.getAnnotations();
		for (IAnnotationBinding a : annotations)
		{
			if (annotation.getName().equals(a.getAnnotationType().getQualifiedName()))
			{
				return a;
			}
		}
		return null;
	}

	public static void addMarker(IJavaElement element, String message, int severity, String MARKER_TYPE)
	{
		try
		{
			if (element instanceof ISourceReference)
			{
				ISourceRange range = ((ISourceReference) element).getSourceRange();
				addMarker(element.getResource(), message, range.getOffset(), range.getLength(), severity, MARKER_TYPE);
			}
		}
		catch (JavaModelException ex)
		{
			ex.printStackTrace();
		}
	}

	public static void addMarker(IResource file, String message, int srcPos, int len, int severity, String MARKER_TYPE)
	{
		try
		{
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.CHAR_START, srcPos);
			marker.setAttribute(IMarker.CHAR_END, srcPos + len);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	public static void addMarker(IResource file, String message, int srcPos, int len, int severity, String MARKER_TYPE, String violation)
	{
		try
		{
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.CHAR_START, srcPos);
			marker.setAttribute(IMarker.CHAR_END, srcPos + len);
			marker.setAttribute(VIOLATION, violation);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	public static void addMarker(IResource file, String message, int srcPos, int len, int severity, String MARKER_TYPE, String violation, String binding)
	{
		try
		{
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.CHAR_START, srcPos);
			marker.setAttribute(IMarker.CHAR_END, srcPos + len);
			marker.setAttribute(VIOLATION, violation);
			marker.setAttribute(BINDING, binding);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	public static boolean isDerivedResource(IFile file, IFile derivedFrom, String MARKER_TYPE) throws CoreException
	{
		return file.exists() && derivedFrom.getProjectRelativePath().toPortableString().equals(file.getPersistentProperty(new QualifiedName(MARKER_TYPE, MARKER_TYPE)));
	}

	public static void createDerivedResource(IFile file, InputStream source, IFile derivedFrom, String MARKER_TYPE) throws CoreException
	{
		createResource(file, source);
		file.setDerived(true);
		file.setPersistentProperty(new QualifiedName(MARKER_TYPE, MARKER_TYPE), derivedFrom.getProjectRelativePath().toPortableString());

	}

	public static void createResource(IFile file, InputStream source) throws CoreException
	{
		if (!file.getProject().exists())
		{
			file.getProject().create(null);
		}
		createParent(file.getParent());
		if (!file.exists())
		{
			file.create(source, true, null);
			file.setDerived(true);
			file.setContents(source, IResource.FORCE, null);
			file.touch(null);
			file.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		else
		{
			byte[] oldSource = null;
			byte[] newSource = null;
			try
			{
				oldSource = Utilities.getStreamToByteArray(file.getContents());
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			try
			{
				newSource = Utilities.getStreamToByteArray(source);
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			if (oldSource == null || !Arrays.equals(oldSource, newSource))
			{
				if (oldSource != null)
				{
					try
					{
					ReplaceEdit edit = new ReplaceEdit(0, oldSource.length, new String(newSource));
					ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
					unit.applyTextEdit(edit, null);
					unit.commitWorkingCopy(false, null);
					}
					catch( Exception ex )
					{
						file.setDerived(true);
						file.setContents(new ByteArrayInputStream(newSource), IResource.FORCE, null);
						file.touch(null);
						file.refreshLocal(IResource.DEPTH_INFINITE, null);						
					}
				}
				else
				{
					file.setDerived(true);
					file.setContents(new ByteArrayInputStream(newSource), IResource.FORCE, null);
					file.touch(null);
					file.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
			}
		}
	}

	public static void createParent(IContainer c) throws CoreException
	{
		if (c instanceof IFolder)
		{
			IFolder f = (IFolder) c;
			if (!f.exists())
			{
				createParent(f.getParent());
				f.create(true, true, null);
			}
		}
	}

	public static String join(String[] parts, String join)
	{
		StringBuilder b = new StringBuilder();
		for (String s : parts)
		{
			b.append(s);
			b.append(join);
		}
		return b.toString();

	}

	public static String getEvent(String handledClass)
	{
		if (handledClass.indexOf("Event") != -1)
		{
			return handledClass;
		}
		if (handledClass.indexOf("Raiser") == -1)
		{
			return handledClass + "Event";
		}
		return handledClass.substring(0, handledClass.lastIndexOf("Raiser")) + "Event";
	}

	public static String getRaiser(String handledClass)
	{
		if (handledClass.indexOf("Raiser") != -1)
		{
			return handledClass;
		}
		if (handledClass.indexOf("Event") == -1)
		{
			return handledClass + "Raiser";
		}
		return handledClass.substring(0, handledClass.lastIndexOf("Event")) + "Raiser";
	}

	public static byte[] getStreamToByteArray(InputStream is) throws IOException
	{
		byte[] bytes = new byte[1024 * 1024 * 4];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
		{
			offset += numRead;
		}
		is.close();
		byte[] ret = new byte[offset];
		System.arraycopy(bytes, 0, ret, 0, ret.length);
		return ret;
	}
}
