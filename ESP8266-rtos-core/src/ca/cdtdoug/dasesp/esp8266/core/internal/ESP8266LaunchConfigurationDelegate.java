package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;

public class ESP8266LaunchConfigurationDelegate extends LaunchConfigurationTargetedDelegate {

	public static final String TYPE_ID = "ca.cdtdoug.dasESP.ESP8266.rtos.core.launchConfigurationType";

	private ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);
	private IToolChainManager tcManager = Activator.getService(IToolChainManager.class);

	private IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getMappedResources()[0].getProject();
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		// Set active build config based on toolchain for target
		Map<String, String> properties = new HashMap<>();
		String os = target.getAttribute(ILaunchTarget.ATTR_OS, ""); //$NON-NLS-1$
		if (!os.isEmpty()) {
			properties.put(IToolChain.ATTR_OS, os);
		}
		String arch = target.getAttribute(ILaunchTarget.ATTR_ARCH, ""); //$NON-NLS-1$
		if (!arch.isEmpty()) {
			properties.put(IToolChain.ATTR_ARCH, arch);
		}
		Collection<IToolChain> tcs = tcManager.getToolChainsMatching(properties);
		if (!tcs.isEmpty()) {
			IToolChain toolChain = tcs.iterator().next();

			IProject project = getProject(configuration);
			ICBuildConfiguration config = configManager.getBuildConfiguration(project, toolChain, "run", monitor); //$NON-NLS-1$

			if (config != null) {
				IProjectDescription desc = project.getDescription();
				desc.setActiveBuildConfig(config.getBuildConfiguration().getName());
				project.setDescription(desc, monitor);
			}
		}

		// proceed with the build
		return superBuildForLaunch(configuration, mode, monitor);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = getProject(configuration);
		return new IProject[] { project };
	}

}
