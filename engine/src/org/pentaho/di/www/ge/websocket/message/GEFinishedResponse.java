package org.pentaho.di.www.ge.websocket.message;

public class GEFinishedResponse extends GEResponse {
	private int errorCount;

	public GEFinishedResponse() {
		super();
    	this.responseType = GEResponseType.REQUEST_UPDATE;
	}

	public GEFinishedResponse(int errorCount) {
		this();
		this.errorCount = errorCount;
	}
}