package edu.mit.star.builderplugin.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class STARBuilder extends IncrementalProjectBuilder
{
	public static final String BUILDER_ID = "edu.mit.star.builderplugin.starBuilder";

	CodeVisitorWrapper wrapper;
	ResourceVisitor resourceVisitor = new ResourceVisitor();
	DeltaVisitor deltaVisitor = new DeltaVisitor();

	@Override
	protected IProject[] build(int kind, @SuppressWarnings("unchecked") Map args, IProgressMonitor monitor) throws CoreException
	{
		wrapper = new CodeVisitorWrapperImpl();
		resourceVisitor.setCodeVisitor(wrapper);
		deltaVisitor.setCodeVisitor(wrapper);

		if (kind == FULL_BUILD)
		{
			fullBuild(monitor, wrapper);
		}
		else
		{
			IResourceDelta delta = getDelta(getProject());
			if (delta == null)
			{
				fullBuild(monitor, wrapper);
			}
			else
			{
				incrementalBuild(delta, monitor, wrapper);
			}
		}
		return null;
	}

	private void fullBuild(final IProgressMonitor monitor, final CodeVisitorWrapper wrapper) throws CoreException
	{
		try
		{
			getProject().accept(resourceVisitor);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor, CodeVisitorWrapper wrapper) throws CoreException
	{
		delta.accept(deltaVisitor);
	}

}
