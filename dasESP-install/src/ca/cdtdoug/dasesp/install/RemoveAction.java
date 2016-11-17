package ca.cdtdoug.dasesp.install;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;

/**
 * Remove Action - recursively remove a path
 *     path - the path
 */
public class RemoveAction extends ProvisioningAction {

	@Override
	public IStatus execute(Map<String, Object> parameters) {
		String pathString = (String) parameters.get("path"); //$NON-NLS-1$
		if (pathString == null) {
			return fail("parameter 'path' is missing");
		}

		Path path = Paths.get(pathString);
		if (!Files.exists(path)) {
			return Status.OK_STATUS;
		}

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			return Status.OK_STATUS;
		} catch (IOException e) {
			return fail("deleting files", e);
		}
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
