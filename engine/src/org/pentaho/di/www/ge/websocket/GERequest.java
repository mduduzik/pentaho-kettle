package org.pentaho.di.www.ge.websocket;

import java.util.HashMap;
import java.util.Map;

public final class GERequest{
	public static final String PARAM_META_OBJECT_ID = "PARAM_META_OBJECT_ID";
	public static final String PARAM_CARTE_OBJECT_ID = "PARAM_CARTE_OBJECT_ID";
	public static final String PARAM_META_NAME = "PARAM_META_NAME";
	public static final String PARAM_REPOSITORY_ID = "PARAM_REPOSITORY_ID";
	public static final String PARAM_REPOSITORY_USERNAME = "PARAM_REPOSITORY_USERNAME";
	public static final String PARAM_REPOSITORY_PWD = "PARAM_REPOSITORY_PWD";
	
	private GERequestType type;
    private Map<String,String> params;

    public GERequest(){
    	params = new HashMap<String,String>();
    }

	public GERequest(GERequestType type, Map<String, String> params) {
		super();
		this.type = type;
		this.params = params;
	}

	public GERequestType getType() {
		return type;
	}

	public void setType(GERequestType type) {
		this.type = type;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	public String getStringParam(String paramName) {
		return params.get(paramName);
	}
    
	public String getRepositoryId() {
		return getStringParam(PARAM_REPOSITORY_ID);
	}
	
	public String getRepositoryUsername() {
		return getStringParam(PARAM_REPOSITORY_USERNAME);
	}
	
	public String getRepositoryPassword() {
		return getStringParam(PARAM_REPOSITORY_PWD);
	}
}