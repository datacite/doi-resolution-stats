package uk.bl.datacitestats.services.query;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Query results, all fields nullable, nulls ignored when serialising.
 * 
 * @author tom
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "count", "year", "month", "day", "doi" })
public class QueryResult {

	public static final Comparator<QueryResult> BY_DATE = new Comparator<QueryResult>() {
		@Override
		public int compare(QueryResult o1, QueryResult o2) {
			return o1.toSimpleDate().compareTo(o2.toSimpleDate());
		}
	};

	private String year;
	private String month;
	private String day;
	private Integer count;
	private String doi;

	public QueryResult(String year, String month, String day, Integer count, String doi) {
		this.year = year;
		setMonth(month);
		setDay(day);
		this.count = count;
		this.doi = doi;
	}

	public QueryResult() {

	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
		if (this.month.length() == 1)
			this.month = "0" + this.month;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
		if (this.day.length() == 1)
			this.day = "0" + this.day;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writer().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public String toSimpleDate() {
		String date = getYear();
		date += "-";
		if (getMonth().length() == 1)
			date += "0";
		date += getMonth();
		if (getDay() != null) {
			date += "-";
			if (getDay().length() == 1)
				date += "0";
			date += getDay();
		}
		return date;
	}

}
