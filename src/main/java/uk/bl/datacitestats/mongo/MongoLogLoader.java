package uk.bl.datacitestats.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import uk.bl.datacitestats.logloader.DataciteLogParser;
import uk.bl.datacitestats.logloader.LogLoadReport;
import uk.bl.datacitestats.logloader.LogLoader;

import com.google.common.collect.Sets;

@Singleton
public class MongoLogLoader implements LogLoader {

	private MongoDAO dao;
	private DataciteLogParser parser;
	Set<String> ignore;

	/**
	 * Cerate a Mongo specific log loader.
	 * 
	 * @param dao
	 * @param parser
	 * @param ignoreip
	 *            a list of ip addresses to skip/ignore
	 */
	@Inject
	public MongoLogLoader(MongoDAO dao, DataciteLogParser parser, @Named("log.ignoreip") String[] ignoreip) {
		this.dao = dao;
		this.parser = parser;
		ignore = Sets.newHashSet(ignoreip);
	}

	/**
	 * Iterates over the stream, parses each line and passes the LogLine to the
	 * dao for persistence.
	 * 
	 */
	@Override
	public LogLoadReport load(InputStream logfile) {
		LogLoadReport report = new LogLoadReport();
		try {
			LineIterator it = IOUtils.lineIterator(logfile, "UTF-8");
			while (it.hasNext()) {
				try {
					MongoLogLine line = parser.parse(MongoLogLine.class, it.nextLine());
					if (!ignore.contains(line.getHost())) {
						dao.putLine(parser.parse(MongoLogLine.class, it.nextLine()));
						report.setLinesAdded(report.getLinesAdded() + 1);
					} else
						report.setLinesIgnored(report.getLinesIgnored() + 1);
				} catch (Exception e) {
					report.setLinesFailed(report.getLinesFailed() + 1);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(logfile);
		}
		return report;
	}

}
