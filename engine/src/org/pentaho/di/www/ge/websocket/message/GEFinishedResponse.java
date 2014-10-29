package org.pentaho.di.www.ge.websocket.message;

public class GEFinishedResponse extends GEResponse {
	private int errorCount;

	public GEFinishedResponse() {
		super();
    	this.responseType = GEResponseType.RUN_FINISHED;
	}

	public GEFinishedResponse(int errorCount) {
		this();
		this.errorCount = errorCount;
	}
	
	public GEFinishedResponse(int errorCount, String carteObjectId) {
		this();
		this.errorCount = errorCount;
		this.carteObjectId = carteObjectId;
	}	
}