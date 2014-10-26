	package org.pentaho.di.www.ge.serialization;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.io.IOException;

public class ValueMetaInterfaceSerializer extends JsonSerializer<ValueMetaInterface>
{

    @Override
    public void serialize(ValueMetaInterface value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        try {
            jgen.writeStartObject();
            jgen.writeStringField("name",value.getName());
            jgen.writeStringField("type",value.getTypeDesc());
            jgen.writeNumberField("length",value.getLength());
            if (value.getType() == ValueMetaInterface.TYPE_NUMBER)
                jgen.writeStringField("format",value.getDecimalFormat().toPattern());
            else if (value.getType() == ValueMetaInterface.TYPE_DATE)
                jgen.writeStringField("format",value.getDateFormat().toPattern());
            else
                jgen.writeStringField("format","");
            jgen.writeStringField("currency",value.getCurrencySymbol());
            jgen.writeStringField("decimal",value.getDecimalSymbol());
            jgen.writeNumberField("trim_type",value.getTrimType());
            jgen.writeNumberField("precision",value.getPrecision());
            jgen.writeEndObject();

        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}