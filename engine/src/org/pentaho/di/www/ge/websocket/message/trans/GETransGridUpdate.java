package org.pentaho.di.www.ge.websocket.message.trans;

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.www.ge.websocket.message.GEBaseUpdateMessage;
import org.pentaho.di.www.ge.websocket.message.GEMessageUpdateType;

public class GETransGridUpdate extends GEBaseUpdateMessage {
	private List<RowMetaAndData> stepLines;

	public GETransGridUpdate() {
		super();
		this.msgUpdateType = GEMessageUpdateType.TRANS_GRID;
	}
	
	public GETransGridUpdate(List<RowMetaAndData> stepLines) {
		this();
		this.stepLines = stepLines;
	}

	public List<RowMetaAndData> getStepLines() {
		return stepLines;
	}

	public void setStepLines(List<RowMetaAndData> stepLines) {
		this.stepLines = stepLines;
	}	
}
