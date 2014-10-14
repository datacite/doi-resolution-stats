package uk.bl.datacitestats.mongo;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.bl.datacitestats.logloader.DataciteLogParser;
import uk.bl.datacitestats.mongo.MongoConnection;
import uk.bl.datacitestats.mongo.MongoDAO;
import uk.bl.datacitestats.mongo.MongoLogLoader;
import uk.bl.datacitestats.mongo.MongoQueryResolver;

import com.google.common.base.Optional;

/** Timezone issue due to provided data mismatches */
public class MongoQueryResolverTest {
	private static MongoConnection conn;
	private static String dbname = "statstest";
	private static String colname = "log";
	private static MongoQueryResolver resolver;

	@BeforeClass
	public static void setup() throws UnknownHostException {
		conn = new MongoConnection("localhost", 27017);
		conn.getClient().dropDatabase(dbname);
		resolver = new MongoQueryResolver(conn, dbname, colname);
		MongoDAO dao = new MongoDAO(conn, dbname, colname);		
		MongoLogLoader loader = new MongoLogLoader(dao, new DataciteLogParser(), new String[] {});
		InputStream s = MongoQueryResolverTest.class.getResourceAsStream("/test.log.anon");
		loader.load(s);
	}
	
	/*
	@Test
	public void testQueryPerMonth() throws UnknownHostException {
		Optional<String> o = Optional.absent();
		List<QueryResult> l = resolver.monthly(o);
		System.out.println(l);
		assertEquals(l.size(),1);
		assertEquals(l.get(0).getCount().intValue(),34593);
		assertEquals(l.get(0).getYear(), "2014");
		assertEquals(l.get(0).getMonth(), "01");
	}

	@Test
	public void testQueryPerDay() throws UnknownHostException {
		Optional<String> o = Optional.absent();
		List<QueryResult> l = resolver.daily(o);
		assertEquals(l.size(),31);
		assertEquals(l.get(0).getYear(), "2014");
		assertEquals(l.get(0).getMonth(), "01");
		assertEquals(l.get(0).getDay(), "30");
	}

	@Test
	public void testQueryPerMonthPrefix() throws UnknownHostException {
		resolver.monthly(Optional.of("10.5284"));
	}

	@Test
	public void testQueryPerDayPrefix() throws UnknownHostException {
		resolver.daily(Optional.of("10.5290/1000000010007"));
	}

	@Test
	public void testQueryPerMonthPerDOI() throws UnknownHostException {
		resolver.monthlyPerDOI("10.5290/1000000010007");
	}
*/
	@Test
	public void testGetAllDOis() throws UnknownHostException {
		assertEquals(20405, resolver.getAllDois().size());
	}

}
