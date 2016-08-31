package ca.cdtdoug.dasesp.esp8266.core.internal;

import java.nio.file.Path;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChainProvider;

public class ESP8266ToolChain extends GCCToolChain {

	public static final String ID = "xtensa-lx106-elf";
	public static final String OS = "RTOS";
	public static final String ARCH = "ESP8266";

	public ESP8266ToolChain(IToolChainProvider provider, Path sdkPath) {
		super(provider, ID, sdkPath.toString(), new Path[] {
				sdkPath.resolve("tools"), sdkPath.resolve("tools/" + ID + "/bin")
		}, ID + "-");
		setProperty(ATTR_OS, OS);
		setProperty(ATTR_ARCH, ARCH);
	}
	
	@Override
	public String getBinaryParserId() {
		return CCorePlugin.PLUGIN_ID + ".ELF"; //$NON-NLS-1$
	}

}
