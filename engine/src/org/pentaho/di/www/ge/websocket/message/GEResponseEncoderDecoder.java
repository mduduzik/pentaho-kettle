package org.pentaho.di.www.ge.websocket.message;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.www.ge.websocket.message.trans.GETransGridUpdateEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.trans.GETransLogUpdateEncoderDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class GEResponseEncoderDecoder implements Encoder<GEResponse, String>, Decoder<String, GEResponse>{
    private final static Logger logger = LoggerFactory.getLogger(GEResponseEncoderDecoder.class);
    private final static ObjectMapper mapper = new ObjectMapper();
    public final static GEResponseEncoderDecoder INSTANCE = new GEResponseEncoderDecoder(); 

    @Override
    public GEResponse decode(final String s){
        try{
        	logger.debug(String.format("Serialized message < %s >", s));
        	
        	ObjectNode obj = (ObjectNode)mapper.readTree(s);
        	
        	GEResponseType rt = GEResponseType.valueOf(obj.get("responseType").getTextValue());

			switch (rt) {
        		case CARTE_TRANS_LIST:
        			return GEListTransformationsResponseEncoderDecoder.INSTANCE.decode(s);
        		case REQUEST_UPDATE:
        			return decodeUpdate(obj,s);
        		default:
        			throw new IllegalArgumentException("Response type "+rt+" not supported by decoder");
        	}
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private GEResponse decodeUpdate(ObjectNode obj, final String s) {
    	GEMessageUpdateType ut = GEMessageUpdateType.valueOf(obj.get("msgUpdateType").getTextValue());
    	
		switch (ut) {
		case TRANS_GRID:
			return GETransGridUpdateEncoderDecoder.INSTANCE.decode(s);
		case TRANS_LOG:
			return GETransLogUpdateEncoderDecoder.INSTANCE.decode(s);	
   		default:
			throw new IllegalArgumentException("Update type "+ut+" not supported by decoder");
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
