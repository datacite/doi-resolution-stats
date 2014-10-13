package uk.bl.datacitestats.mongo;

import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.mongodb.MongoClient;

@Singleton
public class MongoConnection {

	private MongoClient mongoClient;

	/**
	 * Creates a single MongoClient with internal connection pooling that can be
	 * shared between threads.
	 * 
	 * @param host
	 * @param port
	 * @throws UnknownHostException
	 */
	@Inject
	public MongoConnection(@Named("mongoHost") String host, @Named("mongoPort") Integer port)
			throws UnknownHostException {
		mongoClient = new MongoClient(host, port);
	}

	/**
	 * Get the client
	 * 
	 * @return MongClient
	 */
	public MongoClient getClient() {
		return mongoClient;
	}
}
