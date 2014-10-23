package org.pentaho.di.www.ge.websocket;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.www.ge.GraphEditorWebSocketTest;
import org.pentaho.di.www.ge.websocket.message.GEMessageType;
import org.pentaho.di.www.ge.websocket.message.GERequest;
import org.pentaho.di.www.ge.websocket.message.GERequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class GERequestEncoderDecoder implements Encoder<GERequest, String>, Decoder<String, GERequest>{
    private final static Logger logger = LoggerFactory.getLogger(GraphEditorWebSocketTest.class);
    private final static ObjectMapper mapper = new ObjectMapper();

    public final static GERequestEncoderDecoder INSTANCE = new GERequestEncoderDecoder(); 
    
    @Override
    public GERequest decode(final String s){
        try{
        	logger.debug(String.format("Serialized message < %s >", s));
        	
        	//Deal with params map
        	Map<String,String> params = new HashMap<String, String>();
        	ObjectNode obj = (ObjectNode)mapper.readTree(s);
        	GERequestType t = GERequestType.valueOf(obj.get("type").getTextValue());
        	
        	
        	JsonNode paramsObj = obj.get("params");
        	Iterator<String> values = paramsObj.getFieldNames();
        	while (values.hasNext()) {
        		String val = values.next();
				params.put(val, paramsObj.get(val).getTextValue());
        	}

        	GERequest req = new GERequest(t, params);
            req.setParams(params);
            
            return req;
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String encode(final GERequest params){
        try{
            return mapper.writeValueAsString(params);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
