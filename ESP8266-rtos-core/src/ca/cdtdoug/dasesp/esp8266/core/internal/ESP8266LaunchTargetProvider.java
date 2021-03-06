/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ca.cdtdoug.dasesp.esp8266.core.internal;

import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.remote.core.RemoteLaunchTargetProvider;

public class ESP8266LaunchTargetProvider extends RemoteLaunchTargetProvider {

	@Override
	protected String getTypeId() {
		return ESP8266SerialConnection.ID;
	}

	@Override
	protected void initLaunchTarget(ILaunchTarget target) {
		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setAttribute(ILaunchTarget.ATTR_OS, ESP8266ToolChain.OS);
		wc.setAttribute(ILaunchTarget.ATTR_ARCH, ESP8266ToolChain.ARCH);
		wc.save();
	}
	
}
