package uk.bl.datacitestats.logloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Walks a file tree, handing all files it finds to the supplied LogLoader.
 * 
 * @author tom
 *
 */
public class LogMarshaller {

	Logger logger = LoggerFactory.getLogger(LogMarshaller.class);
	private String rootpath;
	private LogLoader loader;

	public LogMarshaller(LogLoader loader, @Named("log.root.path") String rootpath) {
		this.loader = loader;
		this.rootpath = rootpath;
	}

	public LogLoadReport findAndParse() {

		final LogLoadReport report = new LogLoadReport();
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
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					File f = file.toFile();
					logger.debug("File: " + file.toString() + " len " + f.length());
					try {
						LogLoadReport r = loader.load(new FileInputStream(f));
						logger.debug(" -> " + r.toString());
						report.setLinesAdded(report.getLinesAdded() + r.getLinesAdded());
						report.setLinesFailed(report.getLinesFailed() + r.getLinesFailed());
						report.setLinesIgnored(report.getLinesIgnored() + r.getLinesIgnored());
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
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
