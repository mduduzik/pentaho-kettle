package org.pentaho.di.www.ge.websocket.message;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GEResponseEncoderDecoderSupport {
    protected ObjectMapper mapper = new ObjectMapper();


    protected GEResponse decode(GEResponse resp, JsonNode obj){
        try{
        	//Deal with params map
        	Map<String,String> params = new HashMap<String, String>();

        	String errorCount = obj.get("errorCount").isNull()?null:obj.get("errorCount").getTextValue();
        	resp.setErrorCount(Integer.valueOf(errorCount));
        	
        	JsonNode paramsObj = obj.get("params");        	
        	Iterator<String> values = paramsObj.getFieldNames();
        	while (values.hasNext()) {
        		String val = values.next();
				params.put(val, paramsObj.get(val).getTextValue());
        	}
        	resp.setParams(params);

        	
        	if (!obj.get("request").isNull()) {
        		GERequest req = GERequestEncoderDecoder.INSTANCE.decode(obj.get("request").getTextValue());
        		resp.setRequest(req);
        	}
            resp.setParams(params);
            
            return resp;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    public String encode(final GEResponse resp){
        try{
            return mapper.writeValueAsString(resp);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
