package edu.mit.star.builderplugin.codegen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import star.annotations.CodeGenerate;
import star.annotations.GeneratedClass;

public class CodeGenerateImpl
{
	public static final String MARKER_TYPE = CodeGenerateImpl.class.getName();

	public void visit(IFile sourceFile, TypeDeclaration sourceNode)
	{
		try
		{
			sourceFile.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
			if (!sourceNode.isInterface())
			{
				ITypeBinding bindings = sourceNode.resolveBinding();
				if (Utilities.hasAnnotation(bindings, CodeGenerate.class))
				{
					if ((bindings.getQualifiedName() + "_generated").equals(bindings.getSuperclass().getQualifiedName()))
					{
						createGeneratedFor(sourceFile, sourceNode, bindings);
					}
					else
					{
						Utilities.addMarker(sourceFile, bindings.getQualifiedName() + " must extend " + (bindings.getQualifiedName() + "_generated"), sourceNode.getStartPosition(), sourceNode.getLength(), IMarker.SEVERITY_ERROR, MARKER_TYPE);
						createGeneratedFor(sourceFile, sourceNode, bindings);
					}
				}
			}
		}
		catch (CoreException ex)
		{
			ex.printStackTrace();
		}
	}

	private IFile getIFile(IPackageBinding pack, ITypeBinding raiser, IFile sourceFile)
	{
		return sourceFile.getProject().getFile("/generated_src/" + Utilities.join(pack.getNameComponents(), "/") + raiser.getName() + "_generated.java");
	}

	private IAnnotationBinding getSignalAnnotation(IAnnotationBinding[] annotations)
	{
		for (IAnnotationBinding a : annotations)
		{
			if (CodeGenerate.class.getName().equals(a.getAnnotationType().getQualifiedName()))
			{
				return a;
			}
		}
		return null;
	}

	private ITypeBinding getSuperclass(ITypeBinding binding)
	{
		IMemberValuePairBinding[] values = getSignalAnnotation(binding.getAnnotations()).getAllMemberValuePairs();
		for (IMemberValuePairBinding b : values)
		{
			if ("extend".equals(b.getName()))
			{
				return (ITypeBinding) b.getValue();
			}
		}
		return null;
	}

	private InputStream getSource(IPackageBinding pack, ITypeBinding raiser)
    {
		String className = raiser.getName() + "_generated";
		ITypeBinding superclass = getSuperclass(raiser);

		CompilationUnit cls = createNewClass();
		AST ast = cls.getAST() ;
		
		TypeDeclaration typeDeclaration = ast.newTypeDeclaration() ;
		typeDeclaration.setName( ast.newSimpleName( raiser.getName() + "_generated" ) ) ;		
		typeDeclaration.setSuperclassType(ast.newSimpleType(ast.newName(superclass.getQualifiedName())));
		
		
		
		GeneratedClass generatedClass = new star.annotations.GeneratedClass(className, GeneratedClass.PUBLIC);

		generatedClass.addMember(GeneratedClass.PRIVATE | GeneratedClass.STATIC | GeneratedClass.FINAL, "long", "serialVersionUID", "1L");
		generatedClass.setParent(superclass.getQualifiedName());
			
		return new ByteArrayInputStream(generatedClass.getSource().getBytes());
    }

	private void createGeneratedFor(IFile sourceFile, TypeDeclaration sourceNode, ITypeBinding bindings) throws CoreException
	{
		IPackageBinding pack = bindings.getTypeDeclaration().getPackage();
		ITypeBinding raiser = bindings.getTypeDeclaration();
		Utilities.createResource(getIFile(pack, raiser, sourceFile), getSource(pack, bindings));
	}

	private CompilationUnit createNewClass()
	{
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource("".toCharArray()); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(null);

	}
}
