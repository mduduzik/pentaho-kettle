package org.pentaho.di.www.ge.websocket.message;

import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.managed.Decoder;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;

/**
 * Encode a {@link ChatProtocol} into a String
 */
public class JacksonEncoderDecoder implements Encoder<JacksonEncoderDecoder.Encodable, String>, Decoder<String,JacksonEncoderDecoder.Decodable> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String encode(Encodable m) {
        try {
            return mapper.writeValueAsString(m);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public JacksonEncoderDecoder.Decodable decode(final String s){
       return null;
    }
    

    /**
     * Marker interface for Jackson.
     */
    public static interface Encodable {
    }
    
    public static interface Decodable {
    }
}
