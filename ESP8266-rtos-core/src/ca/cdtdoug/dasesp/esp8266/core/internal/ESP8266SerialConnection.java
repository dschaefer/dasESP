/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.serial.core.ISerialPortService;
import org.eclipse.remote.serial.core.SerialPortConnection;

public class ESP8266SerialConnection implements IRemoteConnectionPropertyService, IRemoteConnectionChangeListener {

	public static final String ID = "ca.cdtdoug.dasESP.ESP8266.rtos.core.serialConnectionType";

	private static final Map<IRemoteConnection, ESP8266SerialConnection> connections = new HashMap<>();

	private final IRemoteConnection remoteConnection;
	private SerialPortConnection serialPortService;

	public ESP8266SerialConnection(IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
	}

	public static class Factory implements IRemoteConnection.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			// TODO SerialPortConnection is broken. It creates a new one every
			// call to getService
			if (ESP8266SerialConnection.class.equals(service)) {
				synchronized (connections) {
					ESP8266SerialConnection esp = connections.get(remoteConnection);
					if (esp == null) {
						esp = new ESP8266SerialConnection(remoteConnection);
						connections.put(remoteConnection, esp);
					}
					return (T) esp;
				}
			} else if (IRemoteConnectionPropertyService.class.equals(service)) {
				return (T) remoteConnection.getService(ESP8266SerialConnection.class);
			} else if (ISerialPortService.class.equals(service)) {
				ESP8266SerialConnection esp = getService(remoteConnection, ESP8266SerialConnection.class);
				if (esp.serialPortService == null) {
					esp.serialPortService = (SerialPortConnection) new SerialPortConnection.Factory()
							.getService(remoteConnection, ISerialPortService.class);
				}
				return (T) esp.serialPortService;
			} else if (IRemoteCommandShellService.class.equals(service)) {
				ESP8266SerialConnection esp = getService(remoteConnection, ESP8266SerialConnection.class);
				if (esp.serialPortService == null) {
					esp.serialPortService = (SerialPortConnection) new SerialPortConnection.Factory()
							.getService(remoteConnection, ISerialPortService.class);
				}
				return (T) esp.serialPortService;
			} else {
				return null;
			}
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	@Override
	public String getProperty(String key) {
		if (IRemoteConnection.OS_NAME_PROPERTY.equals(key)) {
			return "RTOS";
		} else if (IRemoteConnection.OS_ARCH_PROPERTY.equals(key)) {
			return "ESP8266";
		} else {
			return null;
		}
	}

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		if (event.getType() == RemoteConnectionChangeEvent.CONNECTION_REMOVED) {
			synchronized (connections) {
				connections.remove(event.getConnection());
			}
		}
	}

}
