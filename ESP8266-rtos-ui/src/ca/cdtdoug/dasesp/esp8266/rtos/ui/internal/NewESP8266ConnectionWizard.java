/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ca.cdtdoug.dasesp.esp8266.rtos.ui.internal;

import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.serial.ui.NewSerialPortConnectionWizard;
import org.eclipse.remote.serial.ui.NewSerialPortConnectionWizardPage;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;

public class NewESP8266ConnectionWizard extends NewSerialPortConnectionWizard implements IRemoteUIConnectionWizard {

	private NewSerialPortConnectionWizardPage page;
	private IRemoteConnectionWorkingCopy workingCopy;

	public NewESP8266ConnectionWizard(Shell shell, IRemoteConnectionType connectionType) {
		super(shell, connectionType);
	}

}
