package uk.bl.datacitestats.logloader;

/**
 * Report POJO returned after attepted LogLoad
 * 
 * @author tom
 * 
 */
public class LogLoadReport {
	private int linesAdded = 0;
	private int linesFailed = 0;
	private int linesIgnored = 0;

	public int getLinesAdded() {
		return linesAdded;
	}

	public void setLinesAdded(int linesAdded) {
		this.linesAdded = linesAdded;
	}

	public int getLinesFailed() {
		return linesFailed;
	}

	public void setLinesFailed(int linesFailed) {
		this.linesFailed = linesFailed;
	}

	public int getLinesIgnored() {
		return linesIgnored;
	}

	public void setLinesIgnored(int linesIgnored) {
		this.linesIgnored = linesIgnored;
	}

	@Override
	public String toString() {
		return "LogLoadReport [linesAdded=" + linesAdded + ", linesFailed=" + linesFailed + ", linesIgnored="
				+ linesIgnored + "]";
	}

}
