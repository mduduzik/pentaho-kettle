package org.pentaho.di.www.ge.websocket;

public final class GEResponse{
	private String carteObjectId;
	private GERequest request;
	private String errorMessage;

	
	
    public GEResponse(String carteObjectId) {
		super();
		this.carteObjectId = carteObjectId;
	}

	public GEResponse(GERequest request){
    	this.request = request;
    }

	public String getCarteObjectId() {
		return carteObjectId;
	}

	public void setCarteObjectId(String carteObjectId) {
		this.carteObjectId = carteObjectId;
	}

	public GERequest getRequest() {
		return request;
	}

	public void setRequest(GERequest request) {
		this.request = request;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}