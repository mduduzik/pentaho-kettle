package org.pentaho.di.www.ge.serialization;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.pentaho.di.core.row.RowMeta;

import java.io.IOException;

/**
 * Created by Mduduzi on 11/7/13.
 */

public class RowMetaSerializer extends JsonSerializer<RowMeta>
{

    @Override
    public void serialize(RowMeta value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

    }
}