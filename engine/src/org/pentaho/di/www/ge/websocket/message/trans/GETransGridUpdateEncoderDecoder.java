package org.pentaho.di.www.ge.websocket.message.trans;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.www.ge.websocket.message.GEUpdateEncoderDecoderSupport;
import java.io.IOException;
import java.util.List;

public class GETransGridUpdateEncoderDecoder extends GEUpdateEncoderDecoderSupport implements Encoder<GETransGridUpdate, String>, Decoder<String, GETransGridUpdate>{

    public final static GETransGridUpdateEncoderDecoder INSTANCE = new GETransGridUpdateEncoderDecoder(); 
    
    @Override
    public GETransGridUpdate decode(final String s){
        try{
        	ObjectNode obj = (ObjectNode)mapper.readTree(s);
        	List<RowMetaAndData> stepLines = (List<RowMetaAndData>) mapper.readValue(obj.get("stepLines").toString(),RowMetaAndData.class);
        	GETransGridUpdate resp = new GETransGridUpdate(stepLines);
            
            return (GETransGridUpdate)super.decode(resp, obj);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

	@Override
	public String encode(GETransGridUpdate resp) {
		return super.encode(resp);
	}
}
