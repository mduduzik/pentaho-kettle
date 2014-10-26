package org.pentaho.di.www.ge.serialization;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.www.ge.util.HTMLUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mduduzi on 11/7/13.
 */

public class RowMetaAndDataListSerializer
{

    public static ObjectNode serialize(ObjectMapper mapper, List<RowMetaAndData> list) throws IOException, JsonProcessingException {
    	ObjectNode res = null;
    	try {
        	res = mapper.createObjectNode();
            /**
             *
             * generate rows and metadata
             *
             */
            HashMap jsonRow;
            Object elm;
            ValueMetaInterface vm;
            Object[] row;
            RowMetaInterface rowMI;
            String obj;

            //-- metadata
            RowMetaAndData rowMeta = list.get(0);
            ObjectNode metaData = mapper.createObjectNode();
            res.put("metaData",metaData);
            
            ArrayNode fields = mapper.createArrayNode();
            metaData.put("fields",fields);
            
            ObjectNode field = null;
            for (ValueMetaInterface metaItem : rowMeta.getRowMeta().getValueMetaList()) {
            	field = mapper.createObjectNode();
            	field.put("name",metaItem.getName());
            	field.put("typeDesc",metaItem.getTypeDesc());
            	field.put("type",metaItem.getType());
            	fields.add(field);
            }
            metaData.put("totalProperty","results");
            metaData.put("root","rows");

            //-- data
            res.put("results", list.size());
            res.put("totalProperty", "results");
            res.put("root", "rows");

            ArrayNode rows = serializeData(mapper, list);
    		res.put("rows", rows);
        } catch (Exception e) {
            throw new IOException(e);
        }
    	
    	return res;
    }

	public static ArrayNode serializeData(ObjectMapper mapper,
			List<RowMetaAndData> list)
			throws KettleValueException {
		Object elm;
		ValueMetaInterface vm;
		Object[] row;
		RowMetaInterface rowMI;
		String obj;
		ArrayNode rows;
		rows = mapper.createArrayNode();
		ObjectNode row_ = null;
		for (RowMetaAndData prevrow : list) {
			row_ = mapper.createObjectNode();
		    row = prevrow.getData();
		    rowMI = prevrow.getRowMeta();
		    for (int i = 0; i < rowMI.getFieldNames().length; i++) {
		        elm = row[i];
		        vm = rowMI.getValueMeta(i);
		        obj = rowMI.getString(row, i);
		        switch(vm.getType()) {
		            case 5:/*TYPE_INTEGER*/
		            	row_.put(vm.getName(), Long.valueOf(obj));
		                break;
		            case 2:/*TYPE_STRING*/
		            	row_.put(vm.getName(), HTMLUtil.escape(obj));
		                break;
		            case 3:/*TYPE_DATE*/
		            	row_.put(vm.getName(), obj);
		                break;
		            case 4:/*TYPE_BOOLEAN*/
		            	row_.put(vm.getName(), Boolean.valueOf(obj));
		                break;
		            case 1:/*TYPE_NUMBER*/
		            case 6:/*TYPE_BIGNUMBER*/
		            	row_.put(vm.getName(), new BigDecimal(obj.toString()));
		                break;
		            default:
		            	row_.put(vm.getName(), obj);
		                break;
		        }
		    }
		    rows.add(row_);
		}
		
		return rows;
	}
}