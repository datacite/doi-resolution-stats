package uk.bl.datacitestats.persist.mongo;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.bl.datacitestats.services.query.LogQueryResolver;
import uk.bl.datacitestats.services.query.QueryResult;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Mongo implementation of the stats query resolver. Uses Mongo Aggregations.
 * 
 * @author tom
 * 
 */
public class MongoQueryResolver implements LogQueryResolver {

	Logger logger = LoggerFactory.getLogger(MongoQueryResolver.class);

	private MongoConnection connection;
	private String db;
	private String collection;

	@Inject
	public MongoQueryResolver(MongoConnection connection, @Named("mongo.log.db") String db,
			@Named("mongo.log.collection") String collection) {
		this.connection = connection;
		this.collection = collection;
		this.db = db;
	}

	@Override
	public List<QueryResult> monthly(Optional<String> prefix) {
		if (prefix.isPresent())
			return pipe(Lists.newArrayList(buildMatch(prefix.get()), projectYM, groupYM, sort));
		else
			return pipe(Lists.newArrayList(projectYM, groupYM, sort));
	}

	@Override
	public List<QueryResult> daily(Optional<String> prefix) {
		if (prefix.isPresent())
			return pipe(Lists.newArrayList(buildMatch(prefix.get()), projectYMD, groupYMD, sort));
		else
			return pipe(Lists.newArrayList(projectYMD, groupYMD, sort));
	}

	@Override
	public List<QueryResult> monthlyPerDOI(String prefix) {
		final List<DBObject> pipeline = Lists.newArrayList(buildMatch(prefix), projectYMbyDOI, groupYMbyDOI, sort);
		return pipe(pipeline);
	}

	@Override
	public List<QueryResult> dailyPerDOI(String prefix) {
		final List<DBObject> pipeline = Lists.newArrayList(buildMatch(prefix), projectYMDbyDOI, groupYMDbyDOI, sort);
		return pipe(pipeline);
	}

	@Override
	public List<QueryResult> totalHits(int limit, Optional<String> prefixOrDOI, Optional<Date> from, Optional<Date> to) {
		return pipe(Lists.newArrayList(buildMatch(prefixOrDOI, from, to), groupTotalHits, sort, new BasicDBObject(
				"$limit", limit)));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getAllDois() {
		return connection.getClient().getDB(db).getCollection(collection).distinct("doi");
	}

	private List<QueryResult> pipe(List<DBObject> pipeline) {
		logger.debug(pipeline.toString());
		AggregationOutput output = connection.getClient().getDB(db).getCollection(collection).aggregate(pipeline);
		List<QueryResult> list = Lists.newLinkedList();
		for (DBObject result : output.results()) {
			DBObject id = (DBObject) result.get("_id");
			QueryResult q = new QueryResult();
			if (id.containsField("year"))
				q.setYear(id.get("year").toString());
			if (id.containsField("month"))
				q.setMonth(id.get("month").toString());
			q.setCount((Integer) result.get("count"));
			if (id.containsField("day"))
				q.setDay(id.get("day").toString());
			if (id.containsField("doi"))
				q.setDoi(id.get("doi").toString());
			list.add(q);
		}
		// TODO: make this mongo based and optional?
		return list;
	}

	private BasicDBObject buildMatch(String prefix) {
		BasicDBObject doi;
		if (prefix.contains("/")) {
			doi = new BasicDBObject("doi", prefix);
		} else {
			BasicDBObject o = new BasicDBObject("$gt", prefix + "/").append("$lt", prefix + "/ZZZZZZZZZZZ");
			doi = new BasicDBObject("doi", o);
		}
		return new BasicDBObject("$match", doi);
	}

	/*
	 * private DBObject buildMatch(String prefix, Optional<Date> from,
	 * Optional<Date> to) { BasicDBObject match = buildMatch(prefix); if
	 * (from.isPresent() || to.isPresent()){ BasicDBObject range = new
	 * BasicDBObject(); if (from.isPresent()) range.append("$gt", from); if
	 * (to.isPresent()) range.append("$lt", to);
	 * ((BasicDBObject)match.get("$match")).append("date", range); } return
	 * match; }
	 */

	private DBObject buildMatch(Optional<String> prefix, Optional<Date> from, Optional<Date> to) {
		BasicDBObject match;
		if (prefix.isPresent())
			match = buildMatch(prefix.get());
		else
			match = new BasicDBObject("$match", new BasicDBObject());

		if (from.isPresent() || to.isPresent()) {
			BasicDBObject range = new BasicDBObject();
			if (from.isPresent())
				range.append("$gt", from.get());
			if (to.isPresent())
				range.append("$lt", to.get());
			((BasicDBObject) match.get("$match")).append("date", range);
		}

		return match;
	}

	// Mongo aggregation functions ============================================
	private static final DBObject year = new BasicDBObject("$year", "$date");
	private static final DBObject month = new BasicDBObject("$month", "$date");
	private static final DBObject day = new BasicDBObject("$dayOfMonth", "$date");

	private static final DBObject projectYM = new BasicDBObject("$project", new BasicDBObject("year", year).append(
			"month", month));

	private static final DBObject projectYMD = new BasicDBObject("$project", new BasicDBObject("year", year).append(
			"month", month).append("day", day));

	private static final DBObject projectYMbyDOI = new BasicDBObject("$project", new BasicDBObject("year", year)
			.append("month", month).append("doi", "$doi"));

	private static final DBObject projectYMDbyDOI = new BasicDBObject("$project", new BasicDBObject("year", year)
			.append("month", month).append("day", day).append("doi", "$doi"));

	private static final DBObject groupYM = new BasicDBObject("$group", new BasicDBObject("_id", new BasicDBObject(
			"year", "$year").append("month", "$month")).append("count", new BasicDBObject("$sum", 1)));

	private static final DBObject groupYMD = new BasicDBObject("$group", new BasicDBObject("_id", new BasicDBObject(
			"year", "$year").append("month", "$month").append("day", "$day")).append("count", new BasicDBObject("$sum",
			1)));

	private static final DBObject groupYMbyDOI = new BasicDBObject("$group", new BasicDBObject("_id",
			new BasicDBObject("year", "$year").append("month", "$month").append("doi", "$doi")).append("count",
			new BasicDBObject("$sum", 1)));

	private static final DBObject groupYMDbyDOI = new BasicDBObject("$group",
			new BasicDBObject("_id", new BasicDBObject("year", "$year").append("month", "$month").append("day", "$day")
					.append("doi", "$doi")).append("count", new BasicDBObject("$sum", 1)));

	private static final DBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));

	private static final DBObject groupTotalHits = new BasicDBObject("$group", new BasicDBObject("_id",
			new BasicDBObject("doi", "$doi")).append("count", new BasicDBObject("$sum", 1)));

	// can we make it two arrays, one ['jan14' etc], one count[], zero empty
	// months?

	// top ten list for a prefix (also really quick without limit!)
	// db.log.aggregate({"$match":{"doi":
	// {"$gt":"10.5290/","$lt":"10.5290/Z"}}},{"$group":{"_id":"$doi","count":{"$sum":1}}},{"$sort":{"count":-1}},{"$limit":10});

	// monthly stats for all DOIs for a given prefix:
	// db.log.aggregate({"$match":{"doi":
	// {"$gt":"10.5290/","$lt":"10.5290/Z"}}},{"$project":{"year":{"$year":"$date"},"month":{"$month":"$date"},"doi":"$doi"}},{"$group":{"_id":{"doi":"$doi","year":"$year","month":"$month"},"count":{"$sum":1}}},{"$sort":{"count":-1}});

	// monthly stats for a prefix
	// db.log.aggregate({"$match":{"doi":
	// {"$gt":"10.5290/","$lt":"10.5290/Z"}}},{"$project":{"year":{"$year":"$date"},"month":{"$month":"$date"},"doi":"$doi"}},{"$group":{"_id":{"year":"$year","month":"$month"},"count":{"$sum":1}}},{"$sort":{"count":-1}});

	// monthly stats for all (takes a while)
	// db.log.aggregate({"$project":{"year":{"$year":"$date"},"month":{"$month":"$date"}}},{"$group":{"_id":{"year":"$year","month":"$month"},"count":{"$sum":1}}},{"$sort":{"count":-1}});

	// daily stats
	// db.log.aggregate({"$project":{"year":{"$year":"$date"},"month":{"$month":"$date"},"day":{"$dayOfMonth":"$date"}}},{"$group":{"_id":{"year":"$year","month":"$month","day":"$day"},"count":{"$sum":1}}},{"$sort":{"count":-1}});

	// daily stats for a prefix
	// db.log.aggregate({"$match":{"doi":
	// {"$gt":"10.5290/","$lt":"10.5290/Z"}}},{"$project":{"year":{"$year":"$date"},"month":{"$month":"$date"},"day":{"$dayOfMonth":"$date"}}},{"$group":{"_id":{"year":"$year","month":"$month","day":"$day"},"count":{"$sum":1}}},{"$sort":{"count":-1}});

	// top ten all time
	// db.log.aggregate({"$group":{"_id":"$doi","count":{"$sum":1}}},{"$sort":{"count":-1}},{"$limit":10});
	// top ten last month might be nice
}
