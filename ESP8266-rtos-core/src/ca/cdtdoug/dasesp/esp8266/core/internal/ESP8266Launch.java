package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.io.IOException;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.TargetedLaunch;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.serial.core.ISerialPortService;

public class ESP8266Launch extends TargetedLaunch {

	private final SerialPort serialPort;
	private boolean wasOpen;
	
	public ESP8266Launch(ILaunchConfiguration launchConfiguration, String mode, ILaunchTarget launchTarget,
			ISourceLocator locator) {
		super(launchConfiguration, mode, launchTarget, locator);
		IRemoteConnection connection = launchTarget.getAdapter(IRemoteConnection.class);
		this.serialPort = connection.getService(ISerialPortService.class).getSerialPort();

		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	public void start() {
		this.wasOpen = serialPort.isOpen();
		if (wasOpen) {
			try {
				serialPort.pause();
			} catch (IOException e) {
				Activator.getDefault().getLog().log(Activator.getStatus(e));
			}
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		super.handleDebugEvents(events);
		if (isTerminated() && wasOpen) {
			try {
				serialPort.resume();
			} catch (IOException e) {
				Activator.getDefault().getLog().log(Activator.getStatus(e));
			}
			wasOpen = false;
		}
	}

}
