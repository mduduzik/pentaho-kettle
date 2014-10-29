package org.pentaho.di.www.ge.websocket.message;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.node.ObjectNode;

public class GEFinishedResponseEncoderDecoder extends GEResponseEncoderDecoderSupport implements Encoder<GEFinishedResponse, String>, Decoder<String, GEFinishedResponse>{
    public final static GEFinishedResponseEncoderDecoder INSTANCE = new GEFinishedResponseEncoderDecoder(); 

    @Override
    public GEFinishedResponse decode(final String s){
        try{
        	ObjectNode obj = (ObjectNode)mapper.readTree(s);

        	GEFinishedResponse resp = new GEFinishedResponse();
            super.decode(resp, obj);
            
            return resp;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

	@Override
	public String encode(GEFinishedResponse resp) {
		return super.encode(resp);
	}
}
