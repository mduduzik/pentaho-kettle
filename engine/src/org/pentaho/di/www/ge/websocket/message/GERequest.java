package org.pentaho.di.www.ge.websocket.message;

import java.util.Map;


public final class GERequest extends GEBaseMessage {
	private GERequestType type;

    public GERequest(){
    	super();
    	this.msgType = GEMessageType.REQUEST;
    }

	public GERequest(Map<String, String> params) {
		this();
		this.params = params;
	}
	
	public GERequest(GERequestType type, Map<String, String> params) {
		this();
		this.type = type;
		this.params = params;
	}

	public GERequestType getType() {
		return type;
	}

	public void setType(GERequestType type) {
		this.type = type;
	}
}