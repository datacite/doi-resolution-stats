package uk.bl.datacitestats.services.loader;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Report POJO returned after attepted LogLoad
 * 
 * @author tom
 * 
 */
public class LogLoadReport {
	private int linesAdded = 0;
	private int linesIgnored = 0;
	Map<Class,Integer> errors = Maps.newHashMap();

	public int getLinesAdded() {
		return linesAdded;
	}

	public void setLinesAdded(int linesAdded) {
		this.linesAdded = linesAdded;
	}

	public int getLinesFailed() {
		int count = 0;
		for (Integer i : errors.values()){
			count+=i;
		}
		return count;
	}
	
	public Map<Class,Integer> getErrors(){
		return errors;
	}
	
	public void logError(Exception e){
		if (errors.containsKey(e.getClass()))
			errors.put(e.getClass(), errors.get(e.getClass())+1);
		else
			errors.put(e.getClass(), 1);
	}

	public int getLinesIgnored() {
		return linesIgnored;
	}

	public void setLinesIgnored(int linesIgnored) {
		this.linesIgnored = linesIgnored;
	}

	@Override
	public String toString() {
		return "LogLoadReport [linesAdded=" + linesAdded + ", linesFailed=" + getLinesFailed() + ", linesIgnored="
				+ linesIgnored + ", errors="
						+ errors+"]";
	}

	public void addErrors(Map<Class, Integer> errors2) {
		for (Class c : errors2.keySet()){
			if (errors.containsKey(c))
				errors.put(c, errors.get(c)+errors2.get(c));
			else
				errors.put(c, errors2.get(c));
		}
	}

}
