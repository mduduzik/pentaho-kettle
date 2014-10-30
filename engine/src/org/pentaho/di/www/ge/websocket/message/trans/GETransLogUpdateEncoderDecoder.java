package org.pentaho.di.www.ge.websocket.message.trans;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.www.ge.websocket.message.GEUpdateEncoderDecoderSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GETransLogUpdateEncoderDecoder extends GEUpdateEncoderDecoderSupport implements Encoder<GETransLogUpdate, String>, Decoder<String, GETransLogUpdate>{

    public final static GETransLogUpdateEncoderDecoder INSTANCE = new GETransLogUpdateEncoderDecoder(); 
    
    @Override
    public GETransLogUpdate decode(final String s){
        try{
        	ObjectNode obj = (ObjectNode)mapper.readTree(s);
        	List<String[]> lines = new ArrayList<String[]>();
        	Iterator<JsonNode> logLinesIt = ((ArrayNode)obj.get("logLines")).iterator();
        	while (logLinesIt.hasNext()) {
        		lines.add(mapper.readValue(logLinesIt.next().toString(),String[].class));
        	}        	
        	
        	GETransLogUpdate resp = new GETransLogUpdate(lines,obj.get("maxIndex").getIntValue());
            
            return (GETransLogUpdate)super.decode(resp, obj);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

	@Override
	public String encode(GETransLogUpdate resp) {
		return super.encode(resp);
	}
}

