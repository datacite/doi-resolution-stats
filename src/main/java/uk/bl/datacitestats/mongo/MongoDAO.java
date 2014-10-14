package uk.bl.datacitestats.mongo;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;

@Singleton
public class MongoDAO {

	private MongoConnection connection;
	private String db;
	private String collection;

	/** Creates a (thread safe) DAO instance.
	 * Ensures collection is indexed on (Date) and (DOI, Date)
	 * 
	 * @param connection
	 * @param db
	 * @param collection
	 */
	@Inject
	public MongoDAO(MongoConnection connection, @Named("mongo.log.db") String db,
			@Named("mongo.log.collection") String collection) {
		this.connection = connection;
		this.collection = collection;
		this.db = db;
		connection.getClient().getDB(db).getCollection(collection).createIndex(new BasicDBObject("date", 1));
		connection.getClient().getDB(db).getCollection(collection)
				.createIndex(new BasicDBObject("doi", 1).append("date", 1), new BasicDBObject("unique", true));
	}

	/**
	 * Add a line to the collection. Skips duplicates doi/date pairs.
	 * 
	 * @param line
	 */
	public void putLine(MongoLogLine line) {
		connection.getClient().getDB(db).getCollection(collection)
				.insert(line.toDBObject(), WriteConcern.UNACKNOWLEDGED);
	}
}
