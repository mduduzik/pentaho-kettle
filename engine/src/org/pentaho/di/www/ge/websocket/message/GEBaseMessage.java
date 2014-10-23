package org.pentaho.di.www.ge.websocket.message;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

public class GEBaseMessage{
	public static final String PARAM_META_OBJECT_ID = "PARAM_META_OBJECT_ID";
	public static final String PARAM_CARTE_OBJECT_ID = "PARAM_CARTE_OBJECT_ID";
	public static final String PARAM_META_NAME = "PARAM_META_NAME";
	public static final String PARAM_REPOSITORY_ID = "PARAM_REPOSITORY_ID";
	public static final String PARAM_REPOSITORY_USERNAME = "PARAM_REPOSITORY_USERNAME";
	public static final String PARAM_REPOSITORY_PWD = "PARAM_REPOSITORY_PWD";
	
	protected GEMessageType msgType;
    protected Map<String,String> params;

    public GEBaseMessage(){
    	params = new HashMap<String,String>();
    }

    
	public GEMessageType getMsgType() {
		return msgType;
	}


	public void setMsgType(GEMessageType msgType) {
		this.msgType = msgType;
	}


	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	@Transient
	public String getStringParam(String paramName) {
		return params.get(paramName);
	}
    
	@Transient
	public String getRepositoryId() {
		return getStringParam(PARAM_REPOSITORY_ID);
	}
	
	@Transient
	public String getRepositoryUsername() {
		return getStringParam(PARAM_REPOSITORY_USERNAME);
	}
	
	@Transient
	public String getRepositoryPassword() {
		return getStringParam(PARAM_REPOSITORY_PWD);
	}
}