package org.pentaho.di.www.ge.websocket.message;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.www.ge.GraphEditorWebSocketTest;
import org.pentaho.di.www.ge.json.CustomObjectMapper;
import org.pentaho.di.www.ge.websocket.message.GEMessageType;
import org.pentaho.di.www.ge.websocket.message.GERequest;
import org.pentaho.di.www.ge.websocket.message.GERequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class GEUpdateEncoderDecoder implements Encoder<GEBaseUpdateMessage, String>, Decoder<String, GEBaseUpdateMessage>{
    private final static Logger logger = LoggerFactory.getLogger(GraphEditorWebSocketTest.class);
    private final static CustomObjectMapper mapper = new CustomObjectMapper();

    public final static GEUpdateEncoderDecoder INSTANCE = new GEUpdateEncoderDecoder(); 
    
    @Override
    public GEBaseUpdateMessage decode(final String s){
        try{
        	logger.debug(String.format("Serialized message < %s >", s));
        	
        	//Deal with params map
        	Map<String,String> params = new HashMap<String, String>();
        	ObjectNode obj = (ObjectNode)mapper.readTree(s);
        	GEMessageUpdateType t = GEMessageUpdateType.valueOf(obj.get("msgUpdateType").getTextValue());
        	
        	
        	JsonNode paramsObj = obj.get("params");
        	Iterator<String> values = paramsObj.getFieldNames();
        	while (values.hasNext()) {
        		String val = values.next();
				params.put(val, paramsObj.get(val).getTextValue());
        	}

        	GEBaseUpdateMessage req = new GEBaseUpdateMessage(t);
            req.setParams(params);
            
            return req;
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String encode(final GEBaseUpdateMessage params){
        try{
            String serialized = mapper.writeValueAsString(params);
        	//logger.debug(String.format("Serialized message < %s >", serialized));
			return serialized;
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
