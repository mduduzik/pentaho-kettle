package org.pentaho.di.www.websockets;


import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public final class ExecTransParamsEncoderDecoder implements Encoder<ExecTransParams, String>, Decoder<String, ExecTransParams>{
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ExecTransParams decode(final String s){
        try{
            return mapper.readValue(s, ExecTransParams.class);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String encode(final ExecTransParams params){
        try{
            return mapper.writeValueAsString(params);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
