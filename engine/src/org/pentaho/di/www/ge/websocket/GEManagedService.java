package org.pentaho.di.www.ge.websocket;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.config.service.PathParam;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.MetaBroadcaster;
import org.pentaho.di.www.BaseWebSocket;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.websocket.message.GEBaseUpdateMessage;
import org.pentaho.di.www.ge.websocket.message.GERequest;
import org.pentaho.di.www.ge.websocket.message.GERequestEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.GEResponse;
import org.pentaho.di.www.ge.websocket.message.GEResponseEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.GEUpdateEncoderDecoder;

import java.io.IOException;

import javax.inject.Inject;

/**
 * GrapEditorDelegate WebSocket - Spoon-lite in Carte
 */
@ManagedService(path = GEManagedService.PATH+"{tenant: [a-zA-Z][a-zA-Z_0-9]*}/"+"service")
public final class GEManagedService extends BaseWebSocket {
	private static Class<?> PKG = GEManagedService.class; //
	
	public final static String PATH = "/ged/";

	@PathParam("tenant")
	private String tenant;
	
    private BroadcasterFactory factory;

    private AtmosphereResourceFactory resourceFactory;

    private MetaBroadcaster metaBroadcaster;

	private GraphEditor ge;

	private String uuid;
    
	/**
	 * Invoked when the connection as been fully established and suspended, e.g
	 * ready for receiving messages.
	 *
	 * @param r
	 *            the atmosphere resource
	 */
	@Ready
	public final void onReady(final AtmosphereResource r) {
		resourceFactory = r.getAtmosphereConfig().framework().atmosphereFactory();
		factory = r.getAtmosphereConfig().getBroadcasterFactory();
		
		logBasic(String.format("GraphEditor %s for tenant %s connected.", r.uuid(), tenant));
		this.uuid = r.uuid();
		ge = new GraphEditor(this);
	}

	/**
	 * Invoked when the client disconnect or when an unexpected closing of the
	 * underlying connection happens.
	 *
	 * @param event
	 *            the event
	 */
	@Disconnect
	public final void onDisconnect(final AtmosphereResourceEvent event) {
		if (event.isCancelled())
			logBasic(String.format("Browser {} unexpectedly closed the connection", event.getResource()
					.uuid()));
		else if (event.isClosedByClient())
			logBasic(String.format("Browser {} closed the connection", event.getResource()
					.uuid()));
	}

/**
     * Simple annotated class that demonstrate how {@link org.atmosphere.config.managed.Encoder} and {@link org.atmosphere.config.managed.Decoder
     * can be used.
     *
     * @param message an instance of {@link ChatMessage }
     * @return the chat message
     * @throws IOException
     */
	@Message(encoders = { GEResponseEncoderDecoder.class }, decoders = { GERequestEncoderDecoder.class })
	public final GEResponse onRequest(final GERequest request)
			throws IOException {
		logBasic("{} just send {}", request.getStringParam(GERequest.PARAM_META_NAME),
				request.getStringParam(GERequest.PARAM_META_OBJECT_ID));
		return ge.handleRequest(request);
	}

	public void broadcast(String subTopic, GEBaseUpdateMessage message) {
		String json = GEUpdateEncoderDecoder.INSTANCE.encode((message));
		AtmosphereResource r = resourceFactory.find(this.uuid);
		logBasic(String.format("Broadcasting %s",json));
		r.getResponse().write(json);
		//Broadcaster broadcast = factory.lookup(GEManagedService.PATH+tenant+"/"+"service/update",true);
		//broadcast.addAtmosphereResource(r);
		//broadcast.broadcast(message);
		//r.getBroadcaster().broadcast(json, r);
	}
}
