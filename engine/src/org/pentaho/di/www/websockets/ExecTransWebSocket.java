package org.pentaho.di.www.websockets;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.pentaho.di.www.BaseWebSocket;

import java.io.IOException;

/**
 * Simple annotated class that demonstrate the power of Atmosphere. This class supports all transports, support
 * message length guarantee, heart beat, message cache thanks to the @ManagedService.
 */
@ManagedService(path = "/chat")
public final class ExecTransWebSocket extends BaseWebSocket {
	  private static Class<?> PKG = ExecTransWebSocket.class; //

    /**
     * Invoked when the connection as been fully established and suspended, e.g ready for receiving messages.
     *
     * @param r the atmosphere resource
     */
    @Ready
    public final void onReady(final AtmosphereResource r){
    	logBasic("Browser {} connected.", r.uuid());
    }

    /**
     * Invoked when the client disconnect or when an unexpected closing of the underlying connection happens.
     *
     * @param event the event
     */
    @Disconnect
    public final void onDisconnect(final AtmosphereResourceEvent event){
        if(event.isCancelled())
        	logBasic("Browser {} unexpectedly disconnected", event.getResource().uuid());
        else if(event.isClosedByClient())
        	logBasic("Browser {} closed the connection", event.getResource().uuid());
    }

    /**
     * Simple annotated class that demonstrate how {@link org.atmosphere.config.managed.Encoder} and {@link org.atmosphere.config.managed.Decoder
     * can be used.
     *
     * @param message an instance of {@link ChatMessage }
     * @return the chat message
     * @throws IOException
     */
    @Message(encoders = {ExecTransParamsEncoderDecoder.class}, decoders = {ExecTransParamsEncoderDecoder.class})
    public final ExecTransParams onMessage(final ExecTransParams message) throws IOException{
    	logBasic("{} just send {}", message.getTransName(), message.getObjectId());
        return message;
    }

}
