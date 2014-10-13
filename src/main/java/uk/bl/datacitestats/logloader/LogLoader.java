package uk.bl.datacitestats.logloader;

import java.io.InputStream;

/**
 * Interface that loads a file into a datastore
 * 
 * @author tom
 * 
 */
public interface LogLoader {

	public LogLoadReport load(InputStream logfile);

}
