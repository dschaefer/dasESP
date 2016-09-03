/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ca.cdtdoug.dasesp.esp8266.rtos.ui.internal;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.serial.ui.NewSerialPortConnectionWizard;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewESP8266ConnectionWizard extends NewSerialPortConnectionWizard implements IRemoteUIConnectionWizard, INewWizard {

	public NewESP8266ConnectionWizard(Shell shell, IRemoteConnectionType connectionType) {
		super(shell, connectionType);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

}
