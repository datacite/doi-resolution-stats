package uk.bl.datacitestats.logloader;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import uk.bl.datacitestats.persist.mongo.MongoConnection;
import uk.bl.datacitestats.persist.mongo.MongoDAO;
import uk.bl.datacitestats.persist.mongo.MongoLogLoader;
import uk.bl.datacitestats.services.loader.DataciteLogParser;
import uk.bl.datacitestats.services.loader.LogLoadReport;
import uk.bl.datacitestats.services.loader.LogMarshaller;

import com.google.common.base.Stopwatch;

/**
 * Will actually load the live database - this is more a script than a test.
 * Needs refactoring into an admin service JUnit annotations commented out for
 * now.
 */
public class LogMarshallerTest {

	MongoLogLoader loader;
	MongoDAO dao;
	MongoConnection conn;
	private static String dbname = "dcstats";
	private static String colname = "log";
	LogMarshaller mar;

	@Before
	public void setup() throws UnknownHostException {
		conn = new MongoConnection("localhost", 27017);
		//conn.getClient().dropDatabase(dbname);
		conn.getClient().getDB(dbname);
		dao = new MongoDAO(conn, dbname, colname);
		String[] ignore = new String[] {};// "131.180.162.34", "188.220.246.245","46.137.86.193", "131.180.77.111" };
		loader = new MongoLogLoader(dao, new DataciteLogParser(), ignore);
		mar = new LogMarshaller(loader, "/Users/tom/Desktop/datacite-stats/resources/cnri");
		mar = new LogMarshaller(loader, "/Users/tom/Desktop/datacite-stats/cnrigz");
	}

	//@Test
	public void testMarshall() {
		Stopwatch sw = Stopwatch.createStarted();
		LogLoadReport report = mar.findAndParse();
		System.out.println("time " + sw.elapsed(TimeUnit.MINUTES) + " minutes");
		System.out.println(report);
		assertEquals(report.getLinesAdded(), 8586946);
		assertEquals(report.getLinesIgnored(), 21019661);
		assertEquals(report.getLinesFailed(), 104060);
	}

}
