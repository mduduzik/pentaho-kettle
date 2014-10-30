package org.pentaho.di.www.ge.websocket;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.config.service.PathParam;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.MetaBroadcaster;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.www.BaseWebSocket;
import org.pentaho.di.www.CarteObjectEntry;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.TransformationMap;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.websocket.message.GERequest;
import org.pentaho.di.www.ge.websocket.message.GERequestEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.GEResponse;
import org.pentaho.di.www.ge.websocket.message.GEResponseEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.trans.GETransGridUpdate;
import org.pentaho.di.www.ge.websocket.message.trans.GETransGridUpdateEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.trans.GETransLogUpdateEncoderDecoder;

import java.io.IOException;
import java.util.List;

/**
 * GrapEditorDelegate WebSocket - Spoon-lite in Carte
 */
@ManagedService(path = GEManagedService.PATH + "service/" + "{tenant: [a-zA-Z][a-zA-Z_0-9]*}")
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
	@Ready(encoders = { GETransLogUpdateEncoderDecoder.class, GETransGridUpdateEncoderDecoder.class })
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
			logBasic(String.format("Browser {} unexpectedly closed the connection", event.getResource().uuid()));
		else if (event.isClosedByClient())
			logBasic(String.format("Browser {} closed the connection", event.getResource().uuid()));
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
	public final GEResponse onRequest(final GERequest request) throws IOException {
		GEResponse resp = null;
		switch (request.getType()) {
			case EXEC_TRANS:
				resp = ge.handleRequest(request);
				break;
		}
		return resp;
	}

	public final SlaveServerStatus getTransServerStatus()
			throws IOException {
		List<CarteObjectEntry> transEntries = getTransformationMap().getTransformationObjects();
		SlaveServerStatus serverStatus = new SlaveServerStatus();
		
		for (CarteObjectEntry entry : transEntries) {
			String name = entry.getName();
			String id = entry.getId();
			Trans trans = getTransformationMap().getTransformation(entry);
			String status = trans.getStatus();

			SlaveServerTransStatus sstatus = new SlaveServerTransStatus(name, id, status);
			sstatus.setPaused(trans.isPaused());
			serverStatus.getTransStatusList().add(sstatus);
		}

		return serverStatus;
	}

	public TransformationMap getTransformationMap() {
		return CarteSingleton.getInstance().getTransformationMap();
	}

	public void broadcast(String subTopic, String json) {
		AtmosphereResource r = resourceFactory.find(this.uuid);
		// logBasic(String.format("Broadcasting %s",json));
		synchronized (r) {
			if (r != null)// Connection still there
				r.getResponse().write(json);
		}

		// Broadcaster broadcast =
		// factory.lookup(GEManagedService.PATH+tenant+"/"+"service/update",true);
		// broadcast.addAtmosphereResource(r);
		// broadcast.broadcast(message);
		// r.getBroadcaster().broadcast(json, r);
	}

	public void sendJsonToClient(String json) {
		AtmosphereResource r = resourceFactory.find(this.uuid);
		// logBasic(String.format("Broadcasting %s",json));
		synchronized (r) {
			if (r != null)// Connection still there
				r.getResponse().write(json);
		}

		// Broadcaster broadcast =
		// factory.lookup(GEManagedService.PATH+tenant+"/"+"service/update",true);
		// broadcast.addAtmosphereResource(r);
		// broadcast.broadcast(message);
		// r.getBroadcaster().broadcast(json, r);
	}

	public void broadcast(GETransGridUpdate message) {
		AtmosphereResource r = resourceFactory.find(this.uuid);
		// logBasic(String.format("Broadcasting %s",json));
		// Broadcaster broadcast =
		// factory.lookup(GEManagedService.PATH+tenant+"/"+"service/update",true);
		// broadcast.addAtmosphereResource(r);
		// broadcast.broadcast(message);
		r.getBroadcaster().broadcast(message, r);
	}
}
