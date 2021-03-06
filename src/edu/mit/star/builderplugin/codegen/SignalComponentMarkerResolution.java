package edu.mit.star.builderplugin.codegen;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class SignalComponentMarkerResolution implements IMarkerResolutionGenerator2
{

	@Override
	public boolean hasResolutions(IMarker marker)
	{
		try
		{
			if (SignalComponent.MARKER_TYPE.equals(marker.getType()))
			{
				Object violation = marker.getAttribute(Utilities.VIOLATION);
				if (violation != null && String.valueOf(violation).equalsIgnoreCase(SignalComponent.EXTENDS_VIOLATION))
				{
					return true;
				}
			}
		}
		catch (CoreException ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public IMarkerResolution[] getResolutions(final IMarker marker)
	{
		return new IMarkerResolution[] { new IMarkerResolution()
		{
			IMarker myMarker = marker;

			@Override
			public void run(IMarker marker)
			{
				try
				{
					ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom((IFile) marker.getResource());
					String binding = marker.getAttribute(Utilities.BINDING, null);
					IJavaElement element = compilationUnit.getType(marker.getAttribute(Utilities.BINDING, null));
					if (element != null)
					{
						Document d = new Document(compilationUnit.getSource());
						ASTParser parser = ASTParser.newParser(AST.JLS3);
						parser.setSource(compilationUnit.getSource().toCharArray());
						CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
						AST ast = astRoot.getAST();
						ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
						TypeDeclaration ifc = (TypeDeclaration) astRoot.types().get(0);
						ITrackedNodePosition ifcLoc = rewrite.track(ifc);
						rewrite.set(ifc, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, ast.newName(new String[] { ifc.getName() + "_generated" }), null);
						for (Object id : astRoot.imports())
						{
							ImportDeclaration imp = (ImportDeclaration) id;
							if (imp.getName().toString().endsWith(ifc.getName() + "_generated"))
							{
								rewrite.remove(imp, null);
							}
						}
						TextEdit edits = rewrite.rewriteAST(d, null);
						UndoEdit undo = null;
						undo = edits.apply(d);
						String newSource = d.get();
						compilationUnit.getBuffer().setContents(newSource);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

			}

			@Override
			public String getLabel()
			{
				try
				{

					return MessageFormat.format("Add \"extends {0}_generated\"", myMarker.getAttribute(Utilities.BINDING));
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				return "Add extends _generated";
			}
		} };
	}

}
