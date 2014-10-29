package org.pentaho.di.www.ge.websocket.message;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.www.SlaveServerStatus;

public class GEListTransformationsResponseEncoderDecoder extends GEResponseEncoderDecoderSupport implements Encoder<GEListTransformationsResponse, String>, Decoder<String, GEListTransformationsResponse>{
    public final static GEListTransformationsResponseEncoderDecoder INSTANCE = new GEListTransformationsResponseEncoderDecoder(); 

    @Override
    public GEListTransformationsResponse decode(final String s){
        try{
        	ObjectNode obj = (ObjectNode)mapper.readTree(s); 
        	JsonNode statusObj = obj.get("transStatus");
        	SlaveServerStatus ss = mapper.readValue(statusObj, SlaveServerStatus.class);
        	
        	GEListTransformationsResponse resp = new GEListTransformationsResponse(ss);
            super.decode(resp, obj);
            
            return resp;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

	@Override
	public String encode(GEListTransformationsResponse resp) {
		return super.encode(resp);
	}
}
