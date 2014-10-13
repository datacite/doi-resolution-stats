package uk.bl.datacitestats.mongo;

import java.util.Date;

import uk.bl.datacitestats.logloader.LogLine;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoLogLine implements LogLine {

	private Date date;
	private String host;
	private String referer;
	private String doi;

	public MongoLogLine() {

	}

	public MongoLogLine(DBObject o) {
		this.date = (Date) o.get("date");
		this.host = (String) o.get("h");
		this.referer = (String) o.get("r");
		this.doi = (String) o.get("doi");
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public DBObject toDBObject() {
		BasicDBObject o = new BasicDBObject();
		if (date != null)
			o.append("date", date);
		if (doi != null)
			o.append("doi", doi);
		if (referer != null)
			o.append("r", referer);
		if (host != null)
			o.append("h", host);
		return o;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((doi == null) ? 0 : doi.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((referer == null) ? 0 : referer.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MongoLogLine other = (MongoLogLine) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (doi == null) {
			if (other.doi != null)
				return false;
		} else if (!doi.equals(other.doi))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (referer == null) {
			if (other.referer != null)
				return false;
		} else if (!referer.equals(other.referer))
			return false;
		return true;
	}

}
