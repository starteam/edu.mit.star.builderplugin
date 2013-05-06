/**
 * 
 */
package edu.mit.star.builderplugin.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.mit.star.builderplugin.codegen.Raiser;
import edu.mit.star.builderplugin.codegen.SignalComponent;

final class CodeVisitor extends ASTVisitor
{
	IFile thisFile;

	public CodeVisitor(IFile file)
	{
		thisFile = file;
	}

	@Override
	public boolean visit(TypeDeclaration node)
	{
		(new Raiser()).generate(thisFile, node);
		(new SignalComponent()).generate(thisFile, node);	
		return false;
	}

}