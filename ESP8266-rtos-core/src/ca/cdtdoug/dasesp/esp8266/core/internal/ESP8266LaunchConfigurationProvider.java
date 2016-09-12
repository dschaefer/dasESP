package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.AbstractLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public class ESP8266LaunchConfigurationProvider extends AbstractLaunchConfigProvider {

	private Map<IProject, ILaunchConfiguration> configs = new HashMap<>();

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		// Make sure it's an ESP8266
		if (target.getAttribute(ILaunchTarget.ATTR_OS, "").equals(ESP8266ToolChain.OS) //$NON-NLS-1$
				&& target.getAttribute(ILaunchTarget.ATTR_ARCH, "").equals(ESP8266ToolChain.ARCH)) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor arg0, ILaunchTarget arg1)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(ESP8266LaunchConfigurationDelegate.TYPE_ID);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchConfiguration config = null;
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			config = configs.get(project);
			if (config == null) {
				config = createLaunchConfiguration(descriptor, target);
				// launch config added will get called below to add it to the
				// configs map
			}
		}
		return config;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		IProject project = descriptor.getAdapter(IProject.class);
		workingCopy.setMappedResources(new IResource[] { project });
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			IProject project = configuration.getMappedResources()[0].getProject();
			configs.put(project, configuration);
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		for (Entry<IProject, ILaunchConfiguration> entry : configs.entrySet()) {
			if (configuration.equals(entry.getValue())) {
				configs.remove(entry.getKey());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			configs.remove(project);
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		// TODO?
	}

}
