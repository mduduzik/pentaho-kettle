package org.pentaho.di.www.ge.websocket;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public final class GEResponseEncoderDecoder implements Encoder<GEResponse, String>, Decoder<String, GEResponse>{
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public GEResponse decode(final String s){
        try{
            return mapper.readValue(s, GEResponse.class);
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
