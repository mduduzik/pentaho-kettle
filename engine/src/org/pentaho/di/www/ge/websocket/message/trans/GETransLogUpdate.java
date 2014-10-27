package org.pentaho.di.www.ge.websocket.message.trans;

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.www.ge.websocket.message.GEBaseUpdateMessage;
import org.pentaho.di.www.ge.websocket.message.GEMessageUpdateType;

public class GETransLogUpdate extends GEBaseUpdateMessage {
	private List<String[]> logLines;

	public GETransLogUpdate() {
		super();
		this.msgUpdateType = GEMessageUpdateType.TRANS_LOG;
	}
	
	public GETransLogUpdate(List<String[]> logLines) {
		this();
		this.logLines = logLines;
	}

	public List<String[]> getLogLines() {
		return logLines;
	}

	public void setLogLines(List<String[]> logLines) {
		this.logLines = logLines;
	}
}
