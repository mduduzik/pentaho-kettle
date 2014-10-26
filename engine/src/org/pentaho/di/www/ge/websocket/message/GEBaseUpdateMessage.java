package org.pentaho.di.www.ge.websocket.message;


public class GEBaseUpdateMessage extends GEResponse {
	
	protected GEMessageUpdateType msgUpdateType;

    public GEBaseUpdateMessage(){
    	super();
    	this.msgType = GEMessageType.UPDATE;
    }
    
    public GEBaseUpdateMessage(GEMessageUpdateType msgUpdateType){
    	this();
    	this.msgUpdateType = msgUpdateType;
    }

	public GEMessageUpdateType getMsgUpdateType() {
		return msgUpdateType;
	}

	public void setMsgUpdateType(GEMessageUpdateType msgUpdateType) {
		this.msgUpdateType = msgUpdateType;
	}
}