/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

public class ESP8266ToolChainProvider implements IToolChainProvider {

	public static final String ID = "ca.cdtdoug.dasESP.ESP8266.rtos.core.toolChainProvider";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException {
		// Auto discover the toolchain in the eclipse directory
		try {
			Path sdkPath = Paths.get(Platform.getInstallLocation().getURL().toURI()).resolve("ESP8266_RTOS_SDK");
			if (Files.exists(sdkPath.resolve("tools"))) {
				try {
					String version = sdkPath.toString();
					if (manager.getToolChain(ID, ESP8266ToolChain.ID, version) == null) {
						manager.addToolChain(new ESP8266ToolChain(this, sdkPath));
					}
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(e.getStatus());
				}
			}			
		} catch (URISyntaxException e) {
			Activator.getDefault().getLog().log(Activator.getStatus(e));
		}
	}

	@Override
	public IToolChain getToolChain(String id, String version) throws CoreException {
		// Version is SDK path
		Path sdkPath = Paths.get(version);
		if (Files.exists(sdkPath.resolve("tools"))) {
			return new ESP8266ToolChain(this, sdkPath);
		} else {
			// Not found
			return null;
		}
	}

}
