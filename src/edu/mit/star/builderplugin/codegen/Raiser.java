package edu.mit.star.builderplugin.codegen;

import static edu.mit.star.builderplugin.codegen.Utilities.addMarker;
import static edu.mit.star.builderplugin.codegen.Utilities.hasAnnotation;
import static edu.mit.star.builderplugin.codegen.Utilities.isImplementingInterface;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import star.annotations.GeneratedClass;

public class Raiser
{
	public static final String MARKER_TYPE = edu.mit.star.builderplugin.codegen.Raiser.class.getName();
	public static final String EXTENDS_VIOLATION = "extend";

	
	public void generate(IFile sourceFile, TypeDeclaration sourceNode)
	{
		try
		{
			sourceFile.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
			if (sourceNode.isInterface())
			{
				ITypeBinding bindings = sourceNode.resolveBinding();
				if (hasAnnotation(bindings, star.annotations.Raiser.class))
				{
					if (bindings.getQualifiedName().endsWith("Raiser"))
					{
						generateSource(bindings, sourceFile);
						checkExtendsRaiser(bindings, sourceFile, sourceNode);
					}
					else
					{
						addMarker(sourceFile, bindings.getName() + " must be end with 'Raiser'. ", sourceNode.getStartPosition(), sourceNode.getLength(), IMarker.SEVERITY_WARNING, MARKER_TYPE);
					}
				}
			}
		}
		catch (CoreException ex)
		{
			ex.printStackTrace();
		}
	}

	private boolean checkExtendsRaiser(ITypeBinding bindings, IFile sourceFile, TypeDeclaration sourceNode)
	{
		boolean hasRaiser = isImplementingInterface( bindings , star.event.Raiser.class ) ;
		if (!hasRaiser)
		{
			Utilities.addMarker(sourceFile, bindings.getName() + " must extend star.event.Raiser", sourceNode.getStartPosition(), sourceNode.getLength(), IMarker.SEVERITY_ERROR, MARKER_TYPE, EXTENDS_VIOLATION);
		}
		return hasRaiser;
	}

	private void generateSource(ITypeBinding bindings, IFile sourceFile) throws CoreException
	{
		ITypeBinding raiser = bindings.getTypeDeclaration();
		IPackageBinding pack = raiser.getPackage();
		IFile eventFile = getIFile(pack, raiser, sourceFile);
		InputStream eventSource = new ByteArrayInputStream(getSource(pack, raiser).getSource().getBytes());
		Utilities.createDerivedResource(eventFile, eventSource, sourceFile, MARKER_TYPE);
	}

	private GeneratedClass getSource(IPackageBinding pack, ITypeBinding raiser)
	{
		GeneratedClass generatedClass = createRaiserClass( pack, raiser ) ;
		addRaiserCode( generatedClass , raiser , pack ) ;
		return generatedClass;
	}

	private GeneratedClass createRaiserClass(IPackageBinding pack, ITypeBinding raiser)
	{
		String className = getName(raiser);
		ITypeBinding superRaiser = getSuperRaiser( raiser ) ;
		String parentEventName = superRaiser != null ? superRaiser.getPackage().getName() + "." + getName( superRaiser ) : "star.event.Event"; 
		GeneratedClass generatedClass = new star.annotations.GeneratedClass(pack.getName() + "." + className, GeneratedClass.PUBLIC);		
		generatedClass.setParent(parentEventName );
		generatedClass.addMember(GeneratedClass.PRIVATE | GeneratedClass.STATIC | GeneratedClass.FINAL, "long", "serialVersionUID", "1L");		
		return generatedClass ;
	}
	
	private void addRaiserCode( GeneratedClass generatedClass , ITypeBinding raiser , IPackageBinding pack)
	{
		String className = getName(raiser);
		String raiserName = raiser.getQualifiedName();
		generatedClass.addMethod(GeneratedClass.PUBLIC, "", className.substring(className.lastIndexOf('.') + 1), new String[] { raiserName }, new String[] { "raiser" }, "super( raiser ) ;");
		generatedClass.addMethod(GeneratedClass.PUBLIC, "", className.substring(className.lastIndexOf('.') + 1), new String[] { "star.event.Raiser", "boolean" }, new String[] { "raiser", "valid" }, "super( raiser , valid ) ;");
		generatedClass.addMethod(GeneratedClass.PUBLIC, "", className.substring(className.lastIndexOf('.') + 1), new String[] { pack.getName() + "." + className }, new String[] { "event" }, "super( event ) ;");
	}

	private ITypeBinding getSuperRaiser(ITypeBinding raiser)
    {
		for( ITypeBinding parent : raiser.getInterfaces() )
		{
			if( isImplementingInterface(parent, star.event.Raiser.class ) )
			{
				return parent ;
			}
		}
	    return null;
    }

	private IFile getIFile(IPackageBinding pack, ITypeBinding raiser, IFile sourceFile)
	{
		return sourceFile.getProject().getFile("/generated_src/" + Utilities.join(pack.getNameComponents(), "/") + getName(raiser) + ".java");
	}

	public void generateRemoved(IFile file)
	{
		if ("java".equals(file.getFileExtension()))
		{
			String path = file.getProjectRelativePath().toString();
			if (path.endsWith("Raiser.java"))
			{
				String src_rel_path = path.substring(path.indexOf('/'));
				try
				{
					IFile eventFile = file.getProject().getFile("/generated_src" + src_rel_path.replace("Raiser.java", "Event.java"));

					if (Utilities.isDerivedResource(eventFile, file, MARKER_TYPE))
					{
						eventFile.delete(true, null);
					}
				}
				catch (CoreException ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	private String getName(ITypeBinding raiser)
	{
		return raiser.getName().replaceAll("Raiser$", "Event");
	}

}
