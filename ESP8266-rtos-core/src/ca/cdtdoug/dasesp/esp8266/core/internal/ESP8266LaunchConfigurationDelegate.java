package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.serial.core.ISerialPortService;

public class ESP8266LaunchConfigurationDelegate extends LaunchConfigurationTargetedDelegate {

	public static final String TYPE_ID = "ca.cdtdoug.dasESP.ESP8266.rtos.core.launchConfigurationType"; //$NON-NLS-1$

	private ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);
	private IToolChainManager tcManager = Activator.getService(IToolChainManager.class);

	private IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getMappedResources()[0].getProject();
	}

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException {
		return new ESP8266Launch(configuration, mode, target, (ISourceLocator) null);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		IProject project = getProject(configuration);
		ICBuildConfiguration buildConfig = getBuildConfiguration(project, mode, target, monitor);
		if (buildConfig != null) {
			IProjectDescription desc = project.getDescription();
			desc.setActiveBuildConfig(buildConfig.getBuildConfiguration().getName());
			project.setDescription(desc, monitor);
		} else {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "No Build Configuration found."));
		}

		// proceed with the build
		return superBuildForLaunch(configuration, mode, monitor);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject(configuration);
		ILaunchTarget target = ((ITargetedLaunch) launch).getLaunchTarget();
		IRemoteConnection connection = target.getAdapter(IRemoteConnection.class);
		SerialPort serialPort = connection.getService(ISerialPortService.class).getSerialPort();
		
		ICBuildConfiguration buildConfig = getBuildConfiguration(project, mode, target, monitor);
		IBinary[] binaries = buildConfig.getBuildOutput();
		IBinary elfFile = null;
		for (IBinary binary : binaries) {
			// take the first one for now
			if (binary.getPath().getFileExtension().equals("elf")) { //$NON-NLS-1$
				elfFile = binary;
				break;
			}
		}
		if (elfFile == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "No binaries found"));
		}
		Path elfPath = Paths.get(elfFile.getLocationURI());

		IToolChain toolChain = buildConfig.getToolChain();
		Path esptool = toolChain.getCommandPath(Paths.get("esptool")); //$NON-NLS-1$

		List<String> command = new ArrayList<>();
		command.add(esptool.toString());

		command.add("-bz"); //$NON-NLS-1$
		command.add("512K"); //$NON-NLS-1$

		command.add("-eo"); //$NON-NLS-1$
		command.add(elfPath.getFileName().toString());
		command.add("-bo"); //$NON-NLS-1$
		command.add("eagle.flash.bin"); //$NON-NLS-1$
		command.add("-bs"); //$NON-NLS-1$
		command.add(".text"); //$NON-NLS-1$
		command.add("-bs"); //$NON-NLS-1$
		command.add(".data"); //$NON-NLS-1$
		command.add("-bs"); //$NON-NLS-1$
		command.add(".rodata"); //$NON-NLS-1$
		command.add("-bc"); //$NON-NLS-1$
		command.add("-ec"); //$NON-NLS-1$

		command.add("-eo"); //$NON-NLS-1$
		command.add(elfPath.getFileName().toString());
		command.add("-es"); //$NON-NLS-1$
		command.add(".irom0.text"); //$NON-NLS-1$
		command.add("eagle.irom0text.bin"); //$NON-NLS-1$
		command.add("-ec"); //$NON-NLS-1$

		command.add("-cp"); //$NON-NLS-1$
		command.add(serialPort.getPortName());
		command.add("-cd"); //$NON-NLS-1$
		command.add("nodemcu"); //$NON-NLS-1$
		// TODO baud rate

		command.add("-cf"); //$NON-NLS-1$
		command.add("eagle.flash.bin"); //$NON-NLS-1$
		command.add("-ca"); //$NON-NLS-1$
		command.add("0x20000"); //$NON-NLS-1$
		command.add("-cf"); //$NON-NLS-1$
		command.add("eagle.irom0text.bin"); //$NON-NLS-1$
		
		StringBuilder cmdStr = new StringBuilder(command.get(0));
		for (int i = 1; i < command.size(); ++i) {
			cmdStr.append(' ');
			cmdStr.append(command.get(i));
		}

		try {
			((ESP8266Launch) launch).start();
			File dir = elfPath.getParent().toFile();
			ProcessBuilder builder = new ProcessBuilder(command).directory(dir);
			buildConfig.setBuildEnvironment(builder.environment());
			Process process = builder.start();
			DebugPlugin.newProcess(launch, process, cmdStr.toString());
		} catch (IOException e) {
			Activator.getDefault().getLog().log(Activator.getStatus(e));
		}
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = getProject(configuration);
		return new IProject[] { project };
	}

	private ICBuildConfiguration getBuildConfiguration(IProject project, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		// Set active build config based on toolchain for target
		Map<String, String> properties = new HashMap<>();
		properties.put(IToolChain.ATTR_OS, ESP8266ToolChain.OS);
		properties.put(IToolChain.ATTR_ARCH, ESP8266ToolChain.ARCH);
		Collection<IToolChain> tcs = tcManager.getToolChainsMatching(properties);
		if (!tcs.isEmpty()) {
			IToolChain toolChain = tcs.iterator().next();
			return configManager.getBuildConfiguration(project, toolChain, "run", monitor); //$NON-NLS-1$
		} else {
			return null;
		}
	}

}
