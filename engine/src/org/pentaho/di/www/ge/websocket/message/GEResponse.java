package org.pentaho.di.www.ge.websocket.message;

public class GEResponse extends GEBaseMessage {
	private String carteObjectId;
	private GERequest request;
	private String errorMessage;
	protected GEResponseType  responseType;
	
	
    public GEResponse(String carteObjectId) {
    	this();
		this.carteObjectId = carteObjectId;
	}
    
    public GEResponse(String carteObjectId, String errorMessage) {
		this();
		this.carteObjectId = carteObjectId;
		this.errorMessage = errorMessage;
	}

	public GEResponse(GERequest request){
		this();
    	this.request = request;
    }

	public GEResponse() {
		super();
    	this.msgType = GEMessageType.RESPONSE;
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

	public GEResponseType getResponseType() {
		return responseType;
	}

	public void setResponseType(GEResponseType responseType) {
		this.responseType = responseType;
	}
	
	
}