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
		properties.put(IToolChain.ATTR_OS, ESP8266ToolChainProvider.OS);
		properties.put(IToolChain.ATTR_ARCH, ESP8266ToolChainProvider.ARCH);
		for (ICMakeToolChainFile file : manager.getToolChainFilesMatching(properties)) {
			addFile(file);
		}
	}

	@Override
	public void handleCMakeToolChainEvent(CMakeToolChainEvent event) {
		switch (event.getType()) {
		case CMakeToolChainEvent.ADDED:
			ICMakeToolChainFile file = event.getToolChainFile();
			addFile(file);
			break;
		}
	}

	private void addFile(ICMakeToolChainFile file) {
		String version = file.getPath().getParent().toString();
		try {
			// This will load up the toolchain
			tcManager.getToolChain(ESP8266ToolChainProvider.ID, ESP8266ToolChainProvider.TCID, version);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

}
