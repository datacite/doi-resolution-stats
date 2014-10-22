package uk.bl.datacitestats.services.loader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface that loads a file into a datastore
 * 
 * @author tom
 * 
 */
public interface LogLoader {

	public LogLoadReport load(InputStream logfile) throws IOException;

}
