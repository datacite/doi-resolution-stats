package uk.bl.datacitestats.query;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Optional;

/** Main service interface for querying logs
 * 
 * @author tom
 *
 */
public interface LogQueryResolver {

	// get a list of results by month for all requests
	public List<QueryResult> monthly(Optional<String> prefix);

	public List<QueryResult> daily(Optional<String> prefix);

	public List<QueryResult> monthlyPerDOI(String prefixOrDOI);

	public List<QueryResult> dailyPerDOI(String prefixOrDOI);

	public List<String> getAllDois();

	public List<QueryResult> totalHits(int limit, Optional<String> prefixOrDOI, Optional<Date> from, Optional<Date> to);

}
