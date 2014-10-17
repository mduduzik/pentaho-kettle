package org.pentaho.di.www.ge.websocket;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public final class GERequestEncoderDecoder implements Encoder<GERequest, String>, Decoder<String, GERequest>{
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public GERequest decode(final String s){
        try{
            return mapper.readValue(s, GERequest.class);
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
