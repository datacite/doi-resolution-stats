package uk.bl.datacitestats.services.loader;

import java.util.Date;

/**
 * Representation of a single log line
 * 
 * @author tom
 * 
 */
public interface LogLine {
	public Date getDate();

	public void setDate(Date date);

	public String getHost();

	public void setHost(String host);

	public String getReferer();

	public void setReferer(String referer);

	public String getDoi();

	public void setDoi(String doi);
	
	public boolean getExists();
	public void setExists(Boolean exists);
}
