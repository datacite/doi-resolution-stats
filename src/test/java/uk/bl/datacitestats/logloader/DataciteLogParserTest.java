package uk.bl.datacitestats.logloader;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

import uk.bl.datacitestats.persist.mongo.MongoLogLine;
import uk.bl.datacitestats.services.loader.DataciteLogParser;
import uk.bl.datacitestats.services.loader.LogLine;

public class DataciteLogParserTest {

	private static final String logLine = "128.178.109.34 HTTP:HDL \"Wed Jan 01 00:06:23 UTC 2014\" 1 1 186ms 10.5075/epfl-thesis-4914 \"300:10.admin/ETH\" \"\"";
	private static final String logLine2 = "203.45.189.102 HTTP:HDL \"Sat Dec 01 12:01:42 EST 2012\" 1 1 60ms 10.5167/uzh-48131 \"300:10.admin/ETH\" \"\"";
	private static final String logLineIP6 = "2607:f388:1082:fff4:7435:92bb:9c5d:e25e HTTP:HDL \"Wed Jan 01 00:01:41 UTC 2014\" 1 1 2ms 10.5075/epfl-thesis-5998 \"300:10.admin/ETH\" \"http://infoscience.epfl.ch/record/190889\"";

	@SuppressWarnings("deprecation")
	@Test
	public void testParse() throws ParseException {
		DataciteLogParser p = new DataciteLogParser();
		LogLine l1 = p.parse(MongoLogLine.class, logLine);
		assertEquals(l1.getDate().toGMTString(), "1 Jan 2014 00:06:23 GMT");
		assertEquals(l1.getDoi(), "10.5075/epfl-thesis-4914");
		assertEquals(l1.getHost(), "128.178.109.34");
		assertEquals(l1.getReferer(), null);

		LogLine l2 = p.parse(MongoLogLine.class, logLine2);
		assertEquals(l2.getDate().toGMTString(), "1 Dec 2012 17:01:42 GMT");
		assertEquals(l2.getDoi(), "10.5167/uzh-48131");
		assertEquals(l2.getHost(), "203.45.189.102");
		assertEquals(l2.getReferer(), null);
		
		LogLine lip6 = p.parse(MongoLogLine.class, logLineIP6);
		assertEquals(lip6.getDate().toGMTString(), "1 Jan 2014 00:01:41 GMT");
		assertEquals(lip6.getDoi(), "10.5075/epfl-thesis-5998");
		assertEquals(lip6.getHost(), "2607:f388:1082:fff4:7435:92bb:9c5d:e25e");
		assertEquals(lip6.getReferer(), "http://infoscience.epfl.ch/record/190889");

	}

}
