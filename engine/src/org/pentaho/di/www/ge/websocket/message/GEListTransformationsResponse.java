package org.pentaho.di.www.ge.websocket.message;

import org.pentaho.di.www.SlaveServerStatus;

public class GEListTransformationsResponse extends GEResponse {
	private SlaveServerStatus transStatus;

	public GEListTransformationsResponse() {
		super();
    	this.responseType = GEResponseType.CARTE_TRANS_LIST;
	}

	
	public GEListTransformationsResponse(SlaveServerStatus transStatus) {
		this();
		this.transStatus = transStatus;
	}	
}