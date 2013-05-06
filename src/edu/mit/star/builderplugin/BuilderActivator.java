package edu.mit.star.builderplugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class BuilderActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.mit.star.builderplugin";

	// The shared instance
	private static BuilderActivator plugin;
	
	/**
	 * The constructor
	 */
	public BuilderActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		System.err.println( "Starting " + PLUGIN_ID );
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		System.err.println( "Stopping " + PLUGIN_ID );
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BuilderActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
