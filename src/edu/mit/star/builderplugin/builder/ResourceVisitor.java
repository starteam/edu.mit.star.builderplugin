/**
 * 
 */
package edu.mit.star.builderplugin.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

class ResourceVisitor implements IResourceVisitor
{
	/**
	 * 
	 */
	private CodeVisitorWrapper codeVisitor;

	void setCodeVisitor(CodeVisitorWrapper codeVisitor)
	{
		this.codeVisitor = codeVisitor;
	}

	public boolean visit(IResource resource)
	{
		if (resource.getType() == IResource.FILE && "java".equals(resource.getFileExtension()))
		{
			codeVisitor.visitCode((IFile) resource);
		}
		return true;
	}
}