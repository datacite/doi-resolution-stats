package uk.bl.datacitestats.mongo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.bl.datacitestats.persist.mongo.MongoConnection;
import uk.bl.datacitestats.persist.mongo.MongoDAO;
import uk.bl.datacitestats.persist.mongo.MongoLogLine;
import uk.bl.datacitestats.persist.mongo.MongoLogLoader;
import uk.bl.datacitestats.services.loader.DataciteLogParser;
import uk.bl.datacitestats.services.loader.LogLoadReport;

public class MongoLogLoaderTest {

	MongoLogLoader loader;
	MongoDAO dao;
	MongoConnection conn;
	private static String dbname = "testlogdb";
	private static String colname = "testcol";

	@Before
	public void setup() throws UnknownHostException {
		conn = new MongoConnection("localhost", 27017);
		conn.getClient().dropDatabase(dbname);
		dao = new MongoDAO(conn, dbname, colname);
		loader = new MongoLogLoader(dao, new DataciteLogParser(), new String[] {});
		conn.getClient().getDB(dbname);
	}

	@After
	public void teardown() {
		conn.getClient().dropDatabase(dbname);
	}

	//69398 lines!
	//68786 success , 612 duplicates
	@Test
	public void testFileLoad() throws IOException {
		InputStream s = this.getClass().getResourceAsStream("/test.log.anon");
		LogLoadReport report = loader.load(s);
		System.out.println(report);
		assertEquals(69398,report.getLinesAdded()+report.getLinesFailed()+report.getLinesIgnored());
		assertEquals(69398,report.getLinesAdded());
		assertEquals(conn.getClient().getDB(dbname).getCollection(colname).count(), report.getLinesAdded());
		MongoLogLine query = new MongoLogLine();
		query.setDoi("10.6084/m9.figshare.886165");
		assertEquals(conn.getClient().getDB(dbname).getCollection(colname).find(query.toDBObject()).count(), 7);
	}

}
