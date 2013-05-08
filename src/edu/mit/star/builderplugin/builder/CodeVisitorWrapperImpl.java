/**
 * 
 */
package edu.mit.star.builderplugin.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import edu.mit.star.builderplugin.codegen.Raiser;

class CodeVisitorWrapperImpl implements CodeVisitorWrapper
{
	public void visitCode(IFile file)
	{
		ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(file);

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(compilationUnit);

		ASTNode node = parser.createAST(null);
		node.accept(new CodeVisitor(file));
	}

	@Override
	public void visitCodeRemoved(IFile file)
	{
		new Raiser().generateRemoved(file);
	}
}