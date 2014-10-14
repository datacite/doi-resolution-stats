package uk.bl.datacitestats.mongo;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.bl.datacitestats.logloader.DataciteLogParser;
import uk.bl.datacitestats.mongo.MongoConnection;
import uk.bl.datacitestats.mongo.MongoDAO;
import uk.bl.datacitestats.mongo.MongoLogLine;
import uk.bl.datacitestats.mongo.MongoLogLoader;

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

	@Test
	public void testFileLoad() {
		InputStream s = this.getClass().getResourceAsStream("/test.log.anon");
		System.out.println(loader.load(s));
		// non dupliocate lines...
		assertEquals(conn.getClient().getDB(dbname).getCollection(colname).count(), 34593);
		MongoLogLine query = new MongoLogLine();
		query.setDoi("10.6084/m9.figshare.886165");
		assertEquals(conn.getClient().getDB(dbname).getCollection(colname).find(query.toDBObject()).count(), 3);
	}

}
