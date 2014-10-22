package uk.bl.datacitestats.mongo;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.sql.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.bl.datacitestats.persist.mongo.MongoConnection;
import uk.bl.datacitestats.persist.mongo.MongoDAO;
import uk.bl.datacitestats.persist.mongo.MongoLogLine;
import uk.bl.datacitestats.persist.mongo.MongoLogLoader;

import com.mongodb.DBObject;

public class MongoDAOTest {

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
	}

	@After
	public void teardown() {
		conn.getClient().dropDatabase(dbname);
	}

	@Test
	public void testPutLine() {
		MongoLogLine l = new MongoLogLine();
		l.setDate(Date.valueOf("2012-02-03"));
		l.setDoi("10.1234/abc");
		l.setHost("1.2.3.4");
		l.setReferer("http://google.com");
		dao.putLine(l);
		DBObject found = conn.getClient().getDB(dbname).getCollection(colname).findOne();
		MongoLogLine l2 = new MongoLogLine(found);
		assertEquals(l, l2);

		l.setReferer(null);
		l.setDoi("10.1234/def");
		dao.putLine(l);
		l2 = new MongoLogLine(conn.getClient().getDB(dbname).getCollection(colname).find(l.toDBObject()).next());
		assertEquals(l, l2);
	}

}
