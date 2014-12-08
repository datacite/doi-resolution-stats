package uk.bl.datacitestats;

import org.restlet.data.Parameter;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.bl.datacitestats.persist.mongo.MongoLogLoader;
import uk.bl.datacitestats.persist.mongo.MongoQueryResolver;
import uk.bl.datacitestats.services.loader.LogLoader;
import uk.bl.datacitestats.services.query.LogQueryResolver;
import uk.bl.datacitestats.services.query.QueryResult;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Configures our dependencies
 * 
 * @author tom
 * 
 */
public class GuiceConfigModule extends AbstractModule {

	Logger logger = LoggerFactory.getLogger(GuiceConfigModule.class);

	public static final String KEY_MONGO_DB = "mongo.log.db";
	public static final String KEY_MONGO_COLLECTION = "mongo.log.collection";
	public static final String KEY_MONGO_HOST = "mongoHost";
	public static final String KEY_MONGO_PORT = "mongoPort";
	
	public static final String KEY_LOG_ROOT_PATH = "log.root.path";
	public static final String KEY_IGNORE_IP = "log.ignore.ip";
	
	private String db = "dcstats";
	private String col = "log";
	private String host = "localhost";
	private String port = "27017";
	
	private String[] ignoreIP = {};//{"131.180.162.34"};
	private String rootPath = "/var/datacite-stats";//"~/Desktop/datacitestats/cnri";
	//{ "131.180.162.34", "188.220.246.245","46.137.86.193", "131.180.77.111" };;


	/**
	 * Can be configured using web.xml by init-param with defined static keys.
	 * "mongo.log.db", "mongo.log.collection","mongoHost","mongoPort"
	 * 
	 * @param parameters
	 */
	public GuiceConfigModule(Series<Parameter> parameters) {
		if (!parameters.getNames().containsAll(
				Sets.newHashSet(new String[] { KEY_MONGO_DB, KEY_MONGO_COLLECTION, KEY_MONGO_HOST, KEY_MONGO_PORT, KEY_LOG_ROOT_PATH, KEY_IGNORE_IP }))) {
			logger.warn("not all config parameters provided, using configured params " + parameters.getNames());
		}
		this.db = parameters.getFirstValue(KEY_MONGO_DB, db);
		this.col = parameters.getFirstValue(KEY_MONGO_COLLECTION, col);
		this.host = parameters.getFirstValue(KEY_MONGO_HOST, host);
		this.port = parameters.getFirstValue(KEY_MONGO_PORT, port);
		this.rootPath = parameters.getFirstValue(KEY_LOG_ROOT_PATH,rootPath);
		if (parameters.getNames().contains(KEY_IGNORE_IP))
			this.ignoreIP = parameters.getValuesArray(KEY_IGNORE_IP);
	}

	/**
	 * TODO: refactor out into web.xml or external properties file
	 * 
	 */
	@Override
	protected void configure() {
		bind(String.class).annotatedWith(Names.named(KEY_MONGO_DB)).toInstance(db);
		bind(String.class).annotatedWith(Names.named(KEY_MONGO_COLLECTION)).toInstance(col);
		bind(String.class).annotatedWith(Names.named(KEY_MONGO_HOST)).toInstance(host);
		bind(String.class).annotatedWith(Names.named(KEY_MONGO_PORT)).toInstance(port);
		
		bind(String.class).annotatedWith(Names.named(KEY_LOG_ROOT_PATH)).toInstance(rootPath);
		bind(String[].class).annotatedWith(Names.named(KEY_IGNORE_IP)).toInstance(ignoreIP);
				
		bind(LogLoader.class).to(MongoLogLoader.class);
		bind(LogQueryResolver.class).to(MongoQueryResolver.class);
	}

}
