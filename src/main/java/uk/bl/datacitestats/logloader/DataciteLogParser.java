package uk.bl.datacitestats.logloader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @see http://sujitpal.blogspot.co.uk/2009/06/some-access-log-parsers.html
 * 
 * @author tom
 * 
 */
public class DataciteLogParser {

	/**
	 * A stripped down version of the NCSA Log parser. There is no attempt to
	 * identify the fields here. The log line is tokenized by whitespace, unless
	 * the current token begins with a quote (ie multi word fields such as
	 * user_agent) or with a square bracket (ie datetime group). Caller is
	 * responsible for identifying which token to use. Using this approach
	 * simplifies the logic a lot, and also ensures that only the processing
	 * that is absolutely necessary get done, ie we are not spending cycles to
	 * parse out the contents of the datetime or the request group unless we
	 * want to. If we do, then the parsing is done in the caller code - at this
	 * point, the extra parsing can be as simple as calling String.split() with
	 * the appropriate delimiters.
	 */
	public List<String> parseToList(String logline) {
		List<String> tokens = new ArrayList<String>();
		StringBuilder buf = new StringBuilder();
		char[] lc = logline.toCharArray();
		boolean inQuotes = false;
		boolean inBrackets = false;
		for (int i = 0; i < lc.length; i++) {
			if (lc[i] == '"') {
				inQuotes = inQuotes ? false : true;
			} else if (lc[i] == '[') {
				inBrackets = true;
			} else if (lc[i] == ']') {
				if (inBrackets) {
					inBrackets = false;
				}
			} else if (lc[i] == ' ' && (!inQuotes) && (!inBrackets)) {
				tokens.add(buf.toString());
				buf = new StringBuilder();
			} else {
				buf.append(lc[i]);
			}
		}
		if (buf.length() > 0) {
			tokens.add(buf.toString());
		}
		return tokens;
	}

	static final SimpleDateFormat dataciteDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

	/**
	 * Parse a line into your implementation of LogLine
	 * 
	 * @param aClass
	 *            your LogLine implementation
	 * @param dataciteLogLine
	 *            a string.
	 * @return the LogLine
	 * @throws ParseException
	 *             if invalid date encountered
	 */
	public <T extends LogLine> T parse(Class<T> type, String dataciteLogLine) throws ParseException {
		List<String> line = parseToList(dataciteLogLine);
		T logline;
		try {
			logline = type.newInstance();
			logline.setHost(line.get(0));
			logline.setDate(dataciteDate.parse(line.get(2)));// need to work
																// with the
																// timezone
																// here!
			logline.setDoi(line.get(6));
			if (line.size() > 8)
				logline.setReferer(line.get(8));
			return logline;
		} catch (Exception e) {
			throw new RuntimeException("must implement zero arg constructor");
		}
	}

}
