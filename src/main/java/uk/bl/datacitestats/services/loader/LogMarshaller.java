package uk.bl.datacitestats.services.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.GZIPInputStream;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walks a file tree, handing all files it finds to the supplied LogLoader.
 * 
 * @author tom
 * 
 */
public class LogMarshaller {

	Logger logger = LoggerFactory.getLogger(LogMarshaller.class);
	private String rootpath;
	private LogLoader loader;
	final LogLoadReport report = new LogLoadReport();

	public LogMarshaller(LogLoader loader, @Named("log.root.path") String rootpath) {
		this.loader = loader;
		this.rootpath = rootpath;
	}

	/** walks a file system, and walks any gz file found therein.
	 * Assumes any files in the file system are datacite logs and can be added to the database.
	 * 
	 * @return a report containing the results of the load.
	 */
	public LogLoadReport findAndParse() {
		try {
			Path startPath = Paths.get(rootpath);
			logger.debug("file walk starting at " + rootpath);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					logger.debug("Dir: " + dir.toString());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					logger.debug("\t->File: " + file.toString());
					InputStream stream = new FileInputStream(file.toFile());
					if (file.toString().endsWith(".gz"))
						stream = new GZIPInputStream(stream);
					LogLoadReport r = loader.load(stream);
					logger.debug(" -> " + r.toString());
					report.setLinesAdded(report.getLinesAdded() + r.getLinesAdded());
					report.addErrors(r.getErrors());
					report.setLinesIgnored(report.getLinesIgnored() + r.getLinesIgnored());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return report;
	}
	
}
