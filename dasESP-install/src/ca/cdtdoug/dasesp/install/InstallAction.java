package ca.cdtdoug.dasesp.install;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;

/**
 * Install action. Parameters:
 *     url:   source URL for the package to download
 *     dest:  destination folder to unpack to
 *     compression: tar.gz, tar.bz2, or zip
 *     strip: number of directories to strip from source
 *     os:    OS this package is for, ignore if not
 *     arch:  ARCH this package is for, ignore if not
 */
public class InstallAction extends ProvisioningAction {

	@SuppressWarnings("nls")
	@Override
	public IStatus execute(Map<String, Object> parameters) {
		String os = (String) parameters.get("os");
		if (os != null && !os.equals(Platform.getOS())) {
			return Status.OK_STATUS;
		}

		String arch = (String) parameters.get("arch");
		if (arch != null && !arch.equals(Platform.getOSArch())) {
			return Status.OK_STATUS;
		}

		String urlString = (String) parameters.get("url");
		if (urlString == null) {
			return fail("parameter 'url' is missing.");
		}

		String compression = (String) parameters.get("compression");
		if (compression == null) {
			if (urlString.endsWith(".tar.gz") | urlString.endsWith(".tgz")) {
				compression = "tar.gz";
			} else if (urlString.endsWith(".tar.bz2")) {
				compression = "tar.bz2";
			} else if (urlString.endsWith(".zip")) {
				compression = "zip";
			} else {
				return fail("Unknown archive type: " + urlString);
			}
		}

		String destString = (String) parameters.get("dest");
		if (destString == null) {
			return fail("parameter 'dest' is missing.");
		}
		Path dest = Paths.get(destString);

		String stripString = (String) parameters.get("strip");
		int strip = 0;
		if (stripString != null) {
			try {
				strip = Integer.parseInt(stripString);
			} catch (NumberFormatException e) {
				return fail("Bad strip number", e);
			}
		}

		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			try (ArchiveInputStream in = getArchive(connection, compression)) {
				if (!Files.exists(dest)) {
					Files.createDirectories(dest);
				}

				for (ArchiveEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
					Path entryPath = Paths.get(entry.getName());
					if (strip > 0) {
						if (entryPath.getNameCount() > strip) {
							entryPath = entryPath.subpath(strip, entryPath.getNameCount());
						} else {
							continue;
						}
					}
					Path destPath = dest.resolve(entryPath);

					if (entry.isDirectory()) {
						Files.createDirectories(destPath);
						continue;
					}

					if (entry instanceof TarArchiveEntry) {
						TarArchiveEntry tarEntry = (TarArchiveEntry) entry;

						if (tarEntry.isSymbolicLink()) {
							Path targetPath = Paths.get(tarEntry.getLinkName());
							Files.createSymbolicLink(destPath, targetPath);
							continue;
						}
					}

					Files.copy(in, destPath);

					if (entry instanceof TarArchiveEntry) {
						int mode = ((TarArchiveEntry) entry).getMode();
						setMode(destPath, mode);
					}
				}
			}
		} catch (IOException e) {
			return fail("Install failed", e);
		}

		return Status.OK_STATUS;
	}

	private void setMode(Path path, int mode) throws IOException {
		Set<PosixFilePermission> perms = new HashSet<>();
		if ((mode & 0400) != 0) {
			perms.add(PosixFilePermission.OWNER_READ);
		}
		if ((mode & 0200) != 0) {
			perms.add(PosixFilePermission.OWNER_WRITE);
		}
		if ((mode & 0100) != 0) {
			perms.add(PosixFilePermission.OWNER_EXECUTE);
		}
		if ((mode & 0040) != 0) {
			perms.add(PosixFilePermission.GROUP_READ);
		}
		if ((mode & 0020) != 0) {
			perms.add(PosixFilePermission.GROUP_WRITE);
		}
		if ((mode & 0010) != 0) {
			perms.add(PosixFilePermission.GROUP_EXECUTE);
		}
		if ((mode & 0004) != 0) {
			perms.add(PosixFilePermission.OTHERS_READ);
		}
		if ((mode & 0002) != 0) {
			perms.add(PosixFilePermission.OTHERS_WRITE);
		}
		if ((mode & 0001) != 0) {
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
		}

		Files.setPosixFilePermissions(path, perms);
	}

	private ArchiveInputStream getArchive(URLConnection connection, String compression) throws IOException {
		InputStream in = connection.getInputStream();
		switch (compression) {
		case "tar.gz": //$NON-NLS-1$
			GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
			return new TarArchiveInputStream(gzipIn);
		case "tar.bz2": //$NON-NLS-1$
			BZip2CompressorInputStream bz2In = new BZip2CompressorInputStream(in);
			return new TarArchiveInputStream(bz2In);
		case "zip": //$NON-NLS-1$
			return new ZipArchiveInputStream(in);
		default:
			// Shouldn't happen, protected by caller
			return null;
		}
	}

	@Override
	public IStatus undo(Map<String, Object> parameters) {
		// TODO Auto-generated method stub
		return Status.OK_STATUS;
	}

	private IStatus fail(String message) {
		return new Status(IStatus.ERROR, "ca.cdtdoug.dasESP.install", message); //$NON-NLS-1$
	}

	private IStatus fail(String message, Exception e) {
		return new Status(IStatus.ERROR, "ca.cdtdoug.dasESP.install", message, e); //$NON-NLS-1$
	}

}
