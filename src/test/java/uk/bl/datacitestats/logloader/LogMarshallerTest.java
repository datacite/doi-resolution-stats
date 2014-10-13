package uk.bl.datacitestats.logloader;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import uk.bl.datacitestats.mongo.MongoConnection;
import uk.bl.datacitestats.mongo.MongoDAO;
import uk.bl.datacitestats.mongo.MongoLogLoader;

import com.google.common.base.Stopwatch;

/**
 * Will actually load the live database - this is more a script than a test.  Needs refactoring into an admin service
 * JUnit annotations commented out for now.
 */
public class LogMarshallerTest {

	MongoLogLoader loader;
	MongoDAO dao;
	MongoConnection conn;
	private static String dbname = "statstest";
	private static String colname = "log";
	LogMarshaller mar;

	//@Before
	public void setup() throws UnknownHostException {
		conn = new MongoConnection("localhost", 27017);
		conn.getClient().getDB(dbname);
		dao = new MongoDAO(conn, dbname, colname);
		loader = new MongoLogLoader(dao, new DataciteLogParser(), new String[] { "131.180.162.34", "188.220.246.245",
				"46.137.86.193", "131.180.77.111" });
		mar = new LogMarshaller(loader, "/Users/tom/Desktop/datacite-stats/resources/cnri");
	}


	//@Test
	public void testMarshall() {
		Stopwatch sw = Stopwatch.createStarted();
		LogLoadReport report = mar.findAndParse();
		System.out.println("time " + sw.elapsed(TimeUnit.MINUTES) + " minutes");
		System.out.println(report);
		assertEquals(report.getLinesAdded(), 687429);
		assertEquals(report.getLinesIgnored(), 3491109);
		assertEquals(report.getLinesFailed(), 15);
	}

}
