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

	public static final String TYPE_ID = "ca.cdtdoug.dasESP.ESP8266.rtos.core.launchConfigurationType";

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
			if (binary.getPath().getFileExtension().equals("elf")) {
				elfFile = binary;
				break;
			}
		}
		Path elfPath = Paths.get(elfFile.getLocationURI());

		IToolChain toolChain = buildConfig.getToolChain();
		Path esptool = toolChain.getCommandPath(Paths.get("esptool"));

		List<String> command = new ArrayList<>();
		command.add(esptool.toString());

		command.add("-bz");
		command.add("512K");

		command.add("-eo");
		command.add(elfPath.getFileName().toString());
		command.add("-bo");
		command.add("eagle.flash.bin");
		command.add("-bs");
		command.add(".text");
		command.add("-bs");
		command.add(".data");
		command.add("-bs");
		command.add(".rodata");
		command.add("-bc");
		command.add("-ec");

		command.add("-eo");
		command.add(elfPath.getFileName().toString());
		command.add("-es");
		command.add(".irom0.text");
		command.add("eagle.irom0text.bin");
		command.add("-ec");

		command.add("-cp");
		command.add(serialPort.getPortName());
		command.add("-cd");
		command.add("nodemcu");
		// TODO baud rate

		command.add("-cf");
		command.add("eagle.flash.bin");
		command.add("-ca");
		command.add("0x20000");
		command.add("-cf");
		command.add("eagle.irom0text.bin");
		
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
			return configManager.getBuildConfiguration(project, toolChain, "run", monitor); //$NON-NLS-1$
		} else {
			return null;
		}
	}

}
