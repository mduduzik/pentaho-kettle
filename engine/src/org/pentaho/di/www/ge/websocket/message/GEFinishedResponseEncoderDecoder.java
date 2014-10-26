package org.pentaho.di.www.ge.websocket.message;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.www.ge.GraphEditorWebSocketTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class GEFinishedResponseEncoderDecoder implements Encoder<GEFinishedResponse, String>, Decoder<String, GEFinishedResponse>{
    private final static Logger logger = LoggerFactory.getLogger(GraphEditorWebSocketTest.class);
    private final static ObjectMapper mapper = new ObjectMapper();
    public final static GEFinishedResponseEncoderDecoder INSTANCE = new GEFinishedResponseEncoderDecoder(); 

    @Override
    public GEFinishedResponse decode(final String s){
        try{
        	logger.debug(String.format("Serialized message < %s >", s));
        	
        	//Deal with params map
        	Map<String,String> params = new HashMap<String, String>();
        	ObjectNode obj = (ObjectNode)mapper.readTree(s);
        	
        	
        	JsonNode paramsObj = obj.get("params");
        	String errorCount = obj.get("errorCount").isNull()?null:obj.get("errorCount").getTextValue();
        	
        	Iterator<String> values = paramsObj.getFieldNames();
        	while (values.hasNext()) {
        		String val = values.next();
				params.put(val, paramsObj.get(val).getTextValue());
        	}

        	GEFinishedResponse resp = new GEFinishedResponse(Integer.valueOf(errorCount));
        	
        	if (!obj.get("request").isNull()) {
        		GERequest req = GERequestEncoderDecoder.INSTANCE.decode(obj.get("request").getTextValue());
        		resp.setRequest(req);
        	}
            resp.setParams(params);
            
            return resp;
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String encode(final GEFinishedResponse params){
        try{
            return mapper.writeValueAsString(params);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
