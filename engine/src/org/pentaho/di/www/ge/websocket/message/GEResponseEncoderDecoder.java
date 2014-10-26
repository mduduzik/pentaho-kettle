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

public final class GEResponseEncoderDecoder implements Encoder<GEResponse, String>, Decoder<String, GEResponse>{
    private final static Logger logger = LoggerFactory.getLogger(GraphEditorWebSocketTest.class);
    private final static ObjectMapper mapper = new ObjectMapper();
    public final static GEResponseEncoderDecoder INSTANCE = new GEResponseEncoderDecoder(); 

    @Override
    public GEResponse decode(final String s){
        try{
        	logger.debug(String.format("Serialized message < %s >", s));
        	
        	//Deal with params map
        	Map<String,String> params = new HashMap<String, String>();
        	ObjectNode obj = (ObjectNode)mapper.readTree(s);
        	
        	
        	JsonNode paramsObj = obj.get("params");
        	String carteObjectId = obj.get("carteObjectId").isNull()?null:obj.get("carteObjectId").getTextValue();
        	String errorMessage = obj.get("errorMessage").isNull()?null:obj.get("errorMessage").getTextValue();//obj.get("errorMessage").getTextValue();
        	
        	Iterator<String> values = paramsObj.getFieldNames();
        	while (values.hasNext()) {
        		String val = values.next();
				params.put(val, paramsObj.get(val).getTextValue());
        	}

        	GEResponse resp = new GEResponse(carteObjectId,errorMessage);
        	
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
    public String encode(final GEResponse params){
        try{
            return mapper.writeValueAsString(params);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
