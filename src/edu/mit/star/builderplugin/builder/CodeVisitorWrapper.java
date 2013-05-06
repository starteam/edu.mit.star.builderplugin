package edu.mit.star.builderplugin.builder;

import org.eclipse.core.resources.IFile;

public interface CodeVisitorWrapper
{

	void visitCode(IFile resource);

	void visitCodeRemoved(IFile resource);
}
