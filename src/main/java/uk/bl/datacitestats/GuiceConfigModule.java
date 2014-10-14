package uk.bl.datacitestats;

import org.restlet.data.Parameter;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.bl.datacitestats.logloader.LogLoader;
import uk.bl.datacitestats.mongo.MongoLogLoader;
import uk.bl.datacitestats.mongo.MongoQueryResolver;
import uk.bl.datacitestats.query.LogQueryResolver;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/** Configures our dependencies
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

	private String db = "dcstats";
	private String col = "log";
	private String host = "localhost";
	private String port = "27017";

	/**
	 * Can be configured using web.xml by init-param with defined static keys.
	 * 	"mongo.log.db", "mongo.log.collection","mongoHost","mongoPort"
	 * 
	 * @param parameters
	 */
	public GuiceConfigModule(Series<Parameter> parameters) {
		if (!parameters.getNames().containsAll(
				Sets.newHashSet(new String[] { KEY_MONGO_DB, KEY_MONGO_COLLECTION, KEY_MONGO_HOST, KEY_MONGO_PORT }))) {
			logger.warn("not all mongo parameters provided, using configured params " + parameters.getNames());
		}
		this.db = parameters.getFirstValue(KEY_MONGO_DB, db);
		this.col = parameters.getFirstValue(KEY_MONGO_COLLECTION, col);
		this.host = parameters.getFirstValue(KEY_MONGO_HOST, host);
		this.port = parameters.getFirstValue(KEY_MONGO_PORT, port);
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

		bind(String.class).annotatedWith(Names.named("log.root.path")).toInstance(
				"/Users/tom/Desktop/datacitestats/cnri");
		String[] ignoreip = { "131.180.162.34" };
		bind(String[].class).annotatedWith(Names.named("log.ignoreip")).toInstance(ignoreip);

		bind(LogLoader.class).to(MongoLogLoader.class);
		bind(LogQueryResolver.class).to(MongoQueryResolver.class);
	}

}
