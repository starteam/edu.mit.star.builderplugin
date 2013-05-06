/**
 * 
 */
package edu.mit.star.builderplugin.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

class DeltaVisitor implements IResourceDeltaVisitor
{
	/**
	 * 
	 */
	private CodeVisitorWrapper codeVisitor;

	void setCodeVisitor(CodeVisitorWrapper codeVisitor)
	{
		this.codeVisitor = codeVisitor;
	}

	public boolean visit(IResourceDelta delta) throws CoreException
	{
		IResource resource = delta.getResource();
		if (resource.getType() == IResource.FILE && "java".equals(resource.getFileExtension()))
		{
			switch (delta.getKind())
			{
			case IResourceDelta.ADDED:
				codeVisitor.visitCode((IFile) resource);
				break;
			case IResourceDelta.REMOVED:
				codeVisitor.visitCodeRemoved((IFile) resource);
				break;
			case IResourceDelta.CHANGED:
				codeVisitor.visitCode((IFile) resource);
				break;
			}
		}
		return true;
	}
}