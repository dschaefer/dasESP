/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ca.cdtdoug.dasesp.esp8266.rtos.ui.internal;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class ESP8266ConnectionUI extends AbstractRemoteUIConnectionService {

	private final IRemoteConnectionType remoteConnectionType;
	
	public ESP8266ConnectionUI(IRemoteConnectionType remoteConnectionType) {
		this.remoteConnectionType = remoteConnectionType;
	}
	
	public static class Factory implements IRemoteConnectionType.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends org.eclipse.remote.core.IRemoteConnectionType.Service> T getService(
				IRemoteConnectionType connectionType, Class<T> service) {
			if (IRemoteUIConnectionService.class.equals(service)) {
				return (T) new ESP8266ConnectionUI(connectionType);
			} else {
				return null;
			}
		}
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return remoteConnectionType;
	}

	@Override
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell) {
		return new NewESP8266ConnectionWizard(shell, remoteConnectionType);
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new ESP8266LabelProvider();
	}
	
}
