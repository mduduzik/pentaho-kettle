package org.pentaho.di.www.ge.websocket.message;

import org.codehaus.jackson.JsonNode;

public class GEUpdateEncoderDecoderSupport extends GEResponseEncoderDecoderSupport {
	@Override
	protected GEResponse decode(GEResponse resp, JsonNode obj) {
		GEBaseUpdateMessage uResp = (GEBaseUpdateMessage)resp;
    	GEMessageUpdateType t = GEMessageUpdateType.valueOf(obj.get("msgUpdateType").getTextValue());
    	uResp.setMsgUpdateType(t);
		return super.decode(resp, obj);
	}
}
