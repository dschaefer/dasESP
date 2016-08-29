package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.io.IOException;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.serial.core.ISerialPortService;

public class ESP8266SerialConnection implements IRemoteConnectionPropertyService {

	public static final String ID = "ca.cdtdoug.dasESP.ESP8266.rtos.core.serialConnectionType";

	private final IRemoteConnection remoteConnection;
	
	public ESP8266SerialConnection(IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
	}

	public static class Factory implements IRemoteConnection.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (ESP8266SerialConnection.class.equals(service)) {
				return (T) new ESP8266SerialConnection(remoteConnection);
			} else if (IRemoteConnectionPropertyService.class.equals(service)) {
				return (T) remoteConnection.getService(ESP8266SerialConnection.class);
			} else {
				return null;
			}
		}
	}

	public void pause() throws IOException {
		remoteConnection.getService(ISerialPortService.class).getSerialPort().pause();
	}
	
	public void resume() throws IOException {
		remoteConnection.getService(ISerialPortService.class).getSerialPort().pause();
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	@Override
	public String getProperty(String key) {
		if (IRemoteConnection.OS_NAME_PROPERTY.equals(key)) {
			return "rtos";
		} else if (IRemoteConnection.OS_ARCH_PROPERTY.equals(key)) {
			return "ESP8266";
		} else {
			return null;
		}
	}

}
