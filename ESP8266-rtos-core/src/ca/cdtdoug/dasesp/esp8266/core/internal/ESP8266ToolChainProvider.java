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
	public static final String TCID = "xtensa-lx106-elf";
	public static final String OS = "RTOS";
	public static final String ARCH = "ESP8266";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException {
		// Auto discover the toolchain in the eclipse directory
		try {
			Path sdkPath = Paths.get(Platform.getInstallLocation().getURL().toURI()).resolve("ESP8266_RTOS_SDK");
			Path toolsPath = sdkPath.resolve("tools");
			Path gccPath = toolsPath.resolve(TCID);
			if (Files.exists(gccPath)) {
				try {
					String version = sdkPath.toString();
					if (manager.getToolChain(ID, TCID, version) == null) {
						manager.addToolChain(newToolChain(sdkPath, toolsPath, gccPath));
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
		Path toolsPath = sdkPath.resolve("tools");
		Path gccPath = toolsPath.resolve(TCID);
		if (Files.exists(gccPath)) {
			try {
				return newToolChain(sdkPath, toolsPath, gccPath);
			} catch (CoreException e) {
				Activator.getDefault().getLog().log(e.getStatus());
			}
		}
		
		// Not found
		return null;
	}

	private GCCToolChain newToolChain(Path sdkPath, Path toolsPath, Path gccPath) throws CoreException {
		String version = sdkPath.toString();
		GCCToolChain tc = new GCCToolChain(this, TCID, version, new Path[] {
				toolsPath, gccPath.resolve("bin")
		}, TCID + "-");
		tc.setProperty(IToolChain.ATTR_OS, OS);
		tc.setProperty(IToolChain.ATTR_ARCH, ARCH);
		return tc;
	}

}
