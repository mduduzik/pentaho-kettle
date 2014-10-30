package org.pentaho.di.www.ge.websocket.message;

public class GEConfirmStartResponse extends GEResponse {
	public GEConfirmStartResponse() {
		super();
    	this.responseType = GEResponseType.RUN_STARTED;
	}

	
	public GEConfirmStartResponse(String carteObjectId) {
		this();
		this.carteObjectId = carteObjectId;
	}	
}