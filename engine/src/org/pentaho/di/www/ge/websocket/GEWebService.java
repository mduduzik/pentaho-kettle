package org.pentaho.di.www.ge.websocket;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.config.service.PathParam;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.pentaho.di.www.BaseWebSocket;

import java.io.IOException;

import javax.inject.Inject;

/**
 * GrapEditorDelegate WebSocket - Spoon-lite in Carte
 */
@ManagedService(path = "/ged/{usertoken: [a-zA-Z][a-zA-Z_0-9]*}")
public final class GEWebService extends BaseWebSocket {
	private static Class<?> PKG = GEWebService.class; //

	@PathParam("usertoken")
	private String usertoken;
	
    @Inject
    private AtmosphereResourceFactory resourceFactory;

	/**
	 * Invoked when the connection as been fully established and suspended, e.g
	 * ready for receiving messages.
	 *
	 * @param r
	 *            the atmosphere resource
	 */
	@Ready
	public final void onReady(final AtmosphereResource r) {
		logBasic("Browser {} connected.", r.uuid());
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
			logBasic("Browser {} unexpectedly disconnected", event
					.getResource().uuid());
		else if (event.isClosedByClient())
			logBasic("Browser {} closed the connection", event.getResource()
					.uuid());
	}

/**
     * Simple annotated class that demonstrate how {@link org.atmosphere.config.managed.Encoder} and {@link org.atmosphere.config.managed.Decoder
     * can be used.
     *
     * @param message an instance of {@link ChatMessage }
     * @return the chat message
     * @throws IOException
     */
	@Message(encoders = { GERequestEncoderDecoder.class }, decoders = { GERequestEncoderDecoder.class })
	public final GERequest onMessage(final GERequest message)
			throws IOException {
/*		logBasic("{} just send {}", message.getTransName(),
				message.getObjectId());*/
		return message;
	}

}
