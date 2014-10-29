package org.pentaho.di.www.ge.websocket.message;

import java.util.Map;


public final class GEListTransformationsRequest extends GEBaseMessage {
	private GERequestType type;

    public GEListTransformationsRequest(){
    	super();
    	this.msgType = GEMessageType.REQUEST;
    }

	public GEListTransformationsRequest(Map<String, String> params) {
		this();
		this.params = params;
	}
	
	public GEListTransformationsRequest(GERequestType type, Map<String, String> params) {
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