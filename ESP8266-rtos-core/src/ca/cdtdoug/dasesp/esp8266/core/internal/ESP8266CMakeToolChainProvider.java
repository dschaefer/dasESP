/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.CMakeToolChainEvent;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainListener;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.ICMakeToolChainProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.CoreException;

public class ESP8266CMakeToolChainProvider implements ICMakeToolChainProvider, ICMakeToolChainListener {

	IToolChainManager tcManager = Activator.getService(IToolChainManager.class);

	@Override
	public void init(ICMakeToolChainManager manager) {
		manager.addListener(this);
		Map<String, String> properties = new HashMap<>();
		properties.put(IToolChain.ATTR_OS, ESP8266ToolChain.OS);
		properties.put(IToolChain.ATTR_ARCH, ESP8266ToolChain.ARCH);
		try {
			for (IToolChain tc : tcManager.getToolChainsMatching(properties)) {
				Path toolChainFile = Paths.get(tc.getVersion()).resolve("toolchain.cmake"); //$NON-NLS-1$
				if (Files.exists(toolChainFile)) {
					ICMakeToolChainFile file = manager.newToolChainFile(toolChainFile);
					file.setProperty(IToolChain.ATTR_OS, ESP8266ToolChain.OS);
					file.setProperty(IToolChain.ATTR_ARCH, ESP8266ToolChain.ARCH);
					manager.addToolChainFile(file);
				}
			}
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	@Override
	public void handleCMakeToolChainEvent(CMakeToolChainEvent event) {
		switch (event.getType()) {
		case CMakeToolChainEvent.ADDED:
			ICMakeToolChainFile file = event.getToolChainFile();
			String version = file.getPath().getParent().toString();
			try {
				// This will load up the toolchain
				tcManager.getToolChain(ESP8266ToolChainProvider.ID, ESP8266ToolChain.ID, version);
			} catch (CoreException e) {
				Activator.getDefault().getLog().log(e.getStatus());
			}
			break;
		}
	}

}
