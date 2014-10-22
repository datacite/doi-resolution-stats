package uk.bl.datacitestats.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.bl.datacitestats.services.query.LogQueryResolver;
import uk.bl.datacitestats.services.query.QueryResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Resource that serves stats representations. Caches results for 12 hours.
 * 
 * Expects {type},{prefix},{suffix} URI attributes - prefix and suffix are
 * optional type is in [hits,daily,monthly] daily and monthly can have
 * ?breakdown=true ?map changes output format to a map and zero fills missing
 * dates.
 * 
 */

public class StatsResource extends SelfInjectingServerResource {

	Logger log = LoggerFactory.getLogger(StatsResource.class);

	@Inject
	LogQueryResolver resolver;
	SimpleDateFormat df = new SimpleDateFormat("YYYY-mm-DD");

	Optional<String> doi = Optional.absent();
	STAT_TYPE type;
	Integer limit = 100;
	boolean breakdown;
	Optional<Date> from = Optional.absent();
	Optional<Date> to = Optional.absent();

	public enum STAT_TYPE {
		DAILY, MONTHLY, HITS;
		public static Optional<STAT_TYPE> fromString(String s) {
			try {
				return Optional.of(STAT_TYPE.valueOf(s.toUpperCase()));
			} catch (IllegalArgumentException | NullPointerException e) {
				return Optional.absent();
			}
		}
	}

	/**
	 * Parse parameters.
	 * 
	 */
	@Override
	public void doInit() {
		super.doInit();
		String prefix = this.getAttribute("prefix");
		Optional<STAT_TYPE> t = STAT_TYPE.fromString(getAttribute("type"));
		if (!t.isPresent())
			this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "type must be hits, daily or monthly");
		else
			type = t.get();

		String suffix = this.getAttribute("suffix");
		breakdown = (this.getQueryValue("breakdown") != null);
		if (this.getQueryValue("limit") != null) {
			try {
				limit = Integer.parseInt(this.getQueryValue("limit"));
			} catch (NumberFormatException e) {
				this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "limit must be an integer");
			}
		}
		if (this.getQueryValue("from") != null) {
			log.info(this.getQueryValue("from"));
			from = Optional.of(dateYMD.parseDateTime(this.getQueryValue("from")).toDate());
			log.info(from.get().toString());
		}
		if (this.getQueryValue("to") != null) {
			log.info(this.getQueryValue(""));
			to = Optional.of(dateYMD.parseDateTime(this.getQueryValue("to")).toDate());
			log.info(to.get().toString());
		}

		if (suffix != null)
			prefix += "/" + suffix;
		doi = Optional.fromNullable(prefix);
	}

	/**
	 * Interprets request, queries back end, return result. Simple.
	 * 
	 * @return
	 */
	@Get("json")
	public List<QueryResult> getStats() {
		List<QueryResult> result = Lists.newArrayList();
		if (result != null)
			return result;

		if (breakdown && doi.isPresent() && type.equals(STAT_TYPE.MONTHLY)) {
			result = resolver.monthlyPerDOI(doi.get());
		} else if (breakdown && doi.isPresent() && type.equals(STAT_TYPE.DAILY)) {
			result = resolver.dailyPerDOI(doi.get());
		} else if (type.equals(STAT_TYPE.MONTHLY)) {
			result = resolver.monthly(doi);
		} else if (type.equals(STAT_TYPE.DAILY)) {
			result = resolver.daily(doi);
		} else if (type.equals(STAT_TYPE.HITS))
			result = resolver.totalHits(limit, doi, from, to);
		else
			setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED, "Bizare request ignored");

		return result;
	}

	/**
	 * Zero filled map date->count
	 * 
	 * @return
	 * @throws ParseException
	 */
	@Get("json?map")
	public Map<DateTime, Integer> asMap() throws ParseException {
		if (this.getQueryValue("breakdown") != null)
			this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "cannot breakdown into a map");
		if (this.getAttribute("type").equals("hits"))
			this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "cannot transform hits into a map");
		return convert(this.getStats());
	}

	/**
	 * Zero filled map date->count
	 * 
	 * @return
	 * @throws ParseException
	 * @throws JsonProcessingException
	 */
	@Get("?csv")
	public StringRepresentation asCSVString() throws ParseException, JsonProcessingException {
		CsvMapper mapper = new CsvMapper();
		List<QueryResult> list = this.getStats();
		CsvSchema schema = mapper.schemaFor(QueryResult.class).withHeader();
		StringRepresentation rep = new StringRepresentation(mapper.writer().withSchema(schema).writeValueAsString(list));
		if (this.type != STAT_TYPE.HITS) {
			Collections.sort(list, QueryResult.BY_DATE);
		}
		rep.setMediaType(MediaType.TEXT_CSV);
		return rep;
	}

	@Get("csv")
	public StringRepresentation asCSV() throws ParseException, JsonProcessingException {
		return asCSVString();
	}

	/*
	 * @Get("csv?csv") public List<QueryResult> asCsv() { if
	 * (this.getQueryValue("breakdown") != null)
	 * this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
	 * "cannot breakdown into a map"); return this.getStats(); }
	 */

	DateTimeFormatter dateYM = DateTimeFormat.forPattern("yyyy-MM");
	DateTimeFormatter dateYMD = DateTimeFormat.forPattern("yyyy-MM-dd");

	/**
	 * Convert to date->count map sorted by date and zero filled
	 * 
	 * @param results
	 * @return
	 * @throws ParseException
	 */
	private Map<DateTime, Integer> convert(List<QueryResult> results) throws ParseException {
		TreeMap<DateTime, Integer> map = Maps.newTreeMap();
		for (QueryResult q : results) {
			DateTime d;
			if (q.getDay() == null) {
				d = dateYM.parseDateTime(q.getYear() + "-" + q.getMonth());
			} else {
				d = dateYMD.parseDateTime(q.getYear() + "-" + q.getMonth() + "-" + q.getDay());
			}
			map.put(d, q.getCount());
		}
		if (map.size()>0)
			if (this.getAttribute("type").equals("daily")) {
				int days = Days.daysBetween(map.firstKey(), map.lastKey()).getDays();
				for (int i = 0; i <= days; i++) {
					DateTime d = map.firstKey().plusDays(i);
					if (!map.containsKey(d))
						map.put(d, 0);
				}
			} else {
				int months = Months.monthsBetween(map.firstKey(), map.lastKey()).getMonths();
				for (int i = 0; i <= months; i++) {
					DateTime d = map.firstKey().plusMonths(i);
					if (!map.containsKey(d))
						map.put(d, 0);
				}
			}
		// can we modify map here to make it take timezones into account?
		// map.remove(map.lastKey());
		return map;
	}
}
