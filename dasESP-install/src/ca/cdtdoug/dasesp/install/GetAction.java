package ca.cdtdoug.dasesp.install;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;

/**
 * Get action gets a single file:
 *    url:  source url
 *    dest: destination file location
 */
public class GetAction extends ProvisioningAction {

	@Override
	public IStatus execute(Map<String, Object> parameters) {
		String urlString = (String) parameters.get("url"); //$NON-NLS-1$
		if (urlString == null) {
			return fail("parameter 'url' missing.");
		}

		String destString = (String) parameters.get("dest"); //$NON-NLS-1$
		if (destString == null) {
			return fail("parameter 'dest' missing.");
		}
		Path dest = Paths.get(destString);

		for (int retries = 5; retries > 0; retries--) {
			try {
				URL url = new URL(urlString);
				try (InputStream in = url.openConnection().getInputStream()) {
					Files.createDirectories(dest.getParent());
					Files.copy(in, dest);
				}
			} catch (IOException e) {
				if (retries > 0) {
					continue;
				} else {
					return fail("Downloading " + urlString, e);
				}
			}
		}

		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(Map<String, Object> parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	private IStatus fail(String message) {
		return new Status(IStatus.ERROR, "ca.cdtdoug.dasESP.install", message); //$NON-NLS-1$
	}

	private IStatus fail(String message, Exception e) {
		return new Status(IStatus.ERROR, "ca.cdtdoug.dasESP.install", message, e); //$NON-NLS-1$
	}

}
