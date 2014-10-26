/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.www.ge;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.html.dom.HTMLDocumentImpl;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.security.Realm;
import org.eclipse.jetty.client.security.SimpleRealmResolver;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.AbstractBuffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.IO;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.www.AddTransServlet;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.GetStatusServlet;
import org.pentaho.di.www.RemoveTransServlet;
import org.pentaho.di.www.SlaveServerStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.WebResult;
import org.pentaho.di.www.ge.websocket.message.GEBaseMessage;
import org.pentaho.di.www.ge.websocket.message.GEBaseUpdateMessage;
import org.pentaho.di.www.ge.websocket.message.GEMessageType;
import org.pentaho.di.www.ge.websocket.message.GERequest;
import org.pentaho.di.www.ge.websocket.message.GERequestEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.GERequestType;
import org.pentaho.di.www.ge.websocket.message.GEResponse;
import org.pentaho.di.www.ge.websocket.message.GEResponseEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.GEUpdateEncoderDecoder;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteGraphEditorWebSocketTest {
    private final static Logger logger = LoggerFactory.getLogger(RemoteGraphEditorWebSocketTest.class);
    private final static ObjectMapper mapper = new ObjectMapper();

	private CarteSingleton carte = CarteSingleton.getInstance();

	private String carteObjectId;

	private AtmosphereClient wAsyncClient;

    private HttpClient _client;
    private Realm _realm;
    private String _baseUrl;
	private RequestBuilder requestBuilder;
	private String transName;

	@Before
	public void before() {
		transName = "CarteUnitTest";
		_baseUrl = "http://localhost:8660";
		
		try {
			removeTestTransInCarteServer();
			
			carteObjectId = uploadTestTransToCarteServer();//
			System.out.println(String.format("Installed trans %s",carteObjectId));
			
			wAsyncClient = ClientFactory.getDefault().newClient(AtmosphereClient.class);
	        String url = _baseUrl+ "/ged/tenant1000/service";
			requestBuilder = wAsyncClient.newRequestBuilder()
	                .method(Request.METHOD.GET)
	                .uri(url)
	                .trackMessageLength(true)
	                .encoder(new Encoder<Object, String>() {
	                    @Override
	                    public String encode(Object data) {
	                        try {
	                            return mapper.writeValueAsString(data);
	                        } catch (IOException e) {
	                            throw new RuntimeException(e);
	                        }
	                    }
	                })
	                .decoder(new Decoder<String, Object>() {
	                    @Override
	                    public Object decode(Event type, String data) {

	                        data = data.trim();

	                        // Padding
	                        if (data.length() == 0) {
	                            return null;
	                        }

	                        if (type.equals(Event.MESSAGE)) {
	                            try {
	                            	logger.debug(String.format("Browser received message %s", data));
	                            	ObjectNode obj = (ObjectNode)mapper.readTree(data);
	                            	Object msgObj = null;
	                            	GEMessageType mt = GEMessageType.valueOf(obj.get("msgType").getTextValue());
	                            	if (mt == GEMessageType.REQUEST)
	                            		msgObj = GERequestEncoderDecoder.INSTANCE.decode(data);
	                            	else if (mt == GEMessageType.RESPONSE)
	                            		msgObj = GEResponseEncoderDecoder.INSTANCE.decode(data);
	                            	else //Update
	                            		msgObj = data;
	                            	return obj;
	                            } catch (Exception e) {
	                            	e.printStackTrace();
	                                logger.debug("Invalid message {}", data);
	                                return null;
	                            }
	                        } else {
	                            return null;
	                        }
	                    }
	                })
	                .transport(Request.TRANSPORT.WEBSOCKET);
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
		

	}

	/* ------------------------------------------------------------ */
    @After
    public void tearDown()
        throws Exception
    {
    }

	
	@Test
	public void testClient() throws IOException  {
		try {
			Socket socket = wAsyncClient.create();
			final CountDownLatch latch = new CountDownLatch(1);
			socket.on("message", new Function<GEResponse>() {
			    @Override
			    public void on(GEResponse t) {
			        logger.info("GEResponse Message: "+t.toString());
			    }
			}).on("message", new Function<String>() {
			    @Override
			    public void on(String t) {
			        logger.info("String Message: "+t.toString());
			    }
			}).on("message", new Function<GEBaseUpdateMessage>() {
			    @Override
			    public void on(GEBaseUpdateMessage t) {
			        logger.info("Update Message: "+t.toString());
			    }
			}).on(new Function<Throwable>() {

			    @Override
			    public void on(Throwable t) {
			        t.printStackTrace();
			    }

			}).on(Event.CLOSE.name(), new Function<String>() {
			    @Override
			    public void on(String t) {
			        logger.info("Connection closed");
			    }
			}).on(Event.OPEN.name(), new Function<String>() {
			            @Override
			            public void on(String t) {
			                logger.info("Connection opened");
			            }
			        })
			        .open(requestBuilder.build());

			//Run a trans
			Map<String,String> params = new HashMap<String,String>();
			params.put(GEBaseMessage.PARAM_CARTE_OBJECT_ID, carteObjectId);
			params.put(GEBaseMessage.PARAM_REPOSITORY_ID, "");
			params.put(GEBaseMessage.PARAM_REPOSITORY_USERNAME, "");
			params.put(GEBaseMessage.PARAM_REPOSITORY_PWD, "");
			GERequest request = new GERequest(GERequestType.EXEC_TRANS,params);
			socket.fire(request);

			latch.await(30, TimeUnit.SECONDS);
			socket.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Node parse(String content) throws SAXException, IOException {
		DOMFragmentParser parser = new DOMFragmentParser();
		HTMLDocument document = new HTMLDocumentImpl();
		DocumentFragment fragment = document.createDocumentFragment();

		InputSource is = new InputSource(new StringReader(content));
		parser.parse(is, fragment);
		return fragment;
	}

	public static Node findTextNode(Node parent, String parentNodeName) {
		List<Node> nodes = flatten(parent, null);
		for (Node node : nodes) {
			if (node.getNodeType() == Node.TEXT_NODE
					&& node.getParentNode().getNodeName()
							.equalsIgnoreCase(parentNodeName)) {
				return node;
			}
		}
		return null;
	}

	// public static Node findNode(Node parent, String nodeName, short nodeType,
	// int index) {
	// List<Node> nodes = flatten(parent, null);
	// for (Node node : nodes) {
	// if (node.getNodeName().equals(nodeName) && node.getNodeType() ==
	// nodeType) {
	// return node;
	// }
	// }
	// return null;
	// }

	public static List<Node> flatten(Node parent, List<Node> nodes) {
		Node child = parent.getFirstChild();
		if (nodes == null) {
			nodes = new ArrayList<Node>();
		}
		nodes.add(parent);
		while (child != null) {
			flatten(child, nodes);
			child = child.getNextSibling();
		}
		return nodes;
	}

	public static void print(Node node, String indent) {
		// System.out.println(indent + node.getClass().getName());
		if (node.getNodeType() == Node.TEXT_NODE
				&& !StringUtils.isEmpty(node.getTextContent().trim())) {
			System.out.println(node.getParentNode().getNodeName());
			System.out.println(node.getNodeName() + node.getTextContent());
		}
		Node child = node.getFirstChild();
		while (child != null) {
			print(child, indent + " ");
			child = child.getNextSibling();
		}
	}

	// CHECKSTYLE:Indentation:OFF
	public Trans generateTestTransformation() {
		RowGeneratorMeta A = new RowGeneratorMeta();
		A.allocate(3);
		A.setRowLimit("1000000");

		A.getFieldName()[0] = "ID";
		A.getFieldType()[0] = ValueMetaBase
				.getTypeDesc(ValueMetaInterface.TYPE_INTEGER);
		A.getFieldLength()[0] = 7;
		A.getValue()[0] = "1234";

		A.getFieldName()[1] = "Name";
		A.getFieldType()[1] = ValueMetaBase
				.getTypeDesc(ValueMetaInterface.TYPE_STRING);
		A.getFieldLength()[1] = 35;
		A.getValue()[1] = "Some name";

		A.getFieldName()[2] = "Last updated";
		A.getFieldType()[2] = ValueMetaBase
				.getTypeDesc(ValueMetaInterface.TYPE_DATE);
		A.getFieldFormat()[2] = "yyyy/MM/dd";
		A.getValue()[2] = "2010/02/09";

		TransMeta transMeta = TransPreviewFactory
				.generatePreviewTransformation(null, A, "A");

		transMeta.setName(transName);
		transMeta.setSizeRowset(25);
		transMeta.setFeedbackSize(5);
		transMeta.setUsingThreadPriorityManagment(false);

		return new Trans(transMeta);
	}

	public String uploadTestTransToCarteServer() {
		String carteObjId = null;

		
		try {
	        startClient(_realm);
	        
	        ContentExchange getExchange = new ContentExchange();
	        String url = _baseUrl+AddTransServlet.CONTEXT_PATH + "?xml=Y";
			getExchange.setURL(url);
	        //getExchange.setRequestHeader("Content-Type", "application/xml");
	        getExchange.setMethod(HttpMethods.GET);
	        getExchange.setRequestHeader("Host", "tester");
	        getExchange.setVersion("HTTP/1.0");

			TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
			Trans trans = generateTestTransformation();
			TransConfiguration transConfig = new TransConfiguration(
					trans.getTransMeta(), transExecConfig);
			
	        final AbstractBuffer cb = new ByteArrayBuffer(transConfig.getXML().getBytes("UTF-8"));
	        getExchange.setRequestContent(cb);
	        
	        _client.send(getExchange);
	        int state = getExchange.waitForDone();
	        
	        String content = "";
	        int responseStatus = getExchange.getResponseStatus();
	        if (responseStatus == HttpStatus.OK_200)
	        {
	            content = getExchange.getResponseContent();
	        }
	    
	        stopClient();
	        

			WebResult webResult = new WebResult( XMLHandler.loadXMLString( content, WebResult.XML_TAG ) );
			Assert.assertEquals(WebResult.STRING_OK, webResult.getResult());

			SlaveServerStatus status = getStatus();
			SlaveServerTransStatus transStatus = status.findTransStatus(
					trans.getName(), null); // find the first one
			Assert.assertNotNull(transStatus);
			Assert.assertFalse(transStatus.isPaused());
			Assert.assertFalse(transStatus.isRunning());
			
			carteObjId = webResult.getId();
			Assert.assertNotNull(carteObjId);
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}

		return carteObjId;
	}
	

	public void removeTestTransInCarteServer() {
		String carteObjId = null;

		
		try {
	        startClient(_realm);
	        
	        ContentExchange getExchange = new ContentExchange();
	        String url = _baseUrl+RemoveTransServlet.CONTEXT_PATH + "?xml=Y&name="+transName;
			getExchange.setURL(url);
	        //getExchange.setRequestHeader("Content-Type", "application/xml");
	        getExchange.setMethod(HttpMethods.GET);
	        getExchange.setRequestHeader("Host", "tester");
	        getExchange.setVersion("HTTP/1.0");

			TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
			Trans trans = generateTestTransformation();
			TransConfiguration transConfig = new TransConfiguration(
					trans.getTransMeta(), transExecConfig);
			
	        final AbstractBuffer cb = new ByteArrayBuffer(transConfig.getXML().getBytes("UTF-8"));
	        getExchange.setRequestContent(cb);
	        
	        _client.send(getExchange);
	        int state = getExchange.waitForDone();
	        
	        String content = "";
	        int responseStatus = getExchange.getResponseStatus();
	        if (responseStatus == HttpStatus.OK_200)
	        {
	            content = getExchange.getResponseContent();
	        }
	    
	        stopClient();
	        

			WebResult webResult = new WebResult( XMLHandler.loadXMLString( content, WebResult.XML_TAG ) );
			//Assert.assertEquals(WebResult.STRING_OK, webResult.getResult());
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}

	public SlaveServerStatus getStatus() {
        ContentExchange getExchange = new ContentExchange();
        getExchange.setURL(_baseUrl+GetStatusServlet.CONTEXT_PATH + "?xml=Y");
        getExchange.setRequestHeader("Host", "tester");
        getExchange.setMethod(HttpMethods.GET);
        getExchange.setVersion("HTTP/1.0");
        
		try {
	        startClient(_realm);
	        
	        _client.send(getExchange);
	        int state = getExchange.waitForDone();
	        
	        String content = "";
	        int responseStatus = getExchange.getResponseStatus();
	        if (responseStatus == HttpStatus.OK_200)
	        {
	            content = getExchange.getResponseContent();
	        }
	    
	        stopClient();
	        
			return SlaveServerStatus.fromXML(content);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
    protected String getResponseBody(HttpURLConnection conn) throws IOException
    {
        InputStream in = null;
        try
        {
            in = conn.getInputStream();
            return IO.toString(in);
        }
        finally
        {
            IO.close(in);
        }
    }
    
    
    /* ------------------------------------------------------------ */
    protected void startClient(Realm realm)
        throws Exception
    {
        _client = new HttpClient();
        configureClient(_client);
        
        if (realm != null)
            _client.setRealmResolver(new SimpleRealmResolver(realm));
        
        _client.start();
    }
    
    /* ------------------------------------------------------------ */
    protected void configureClient(HttpClient client)
        throws Exception
    {
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
    }

    /* ------------------------------------------------------------ */
    protected void stopClient()
        throws Exception
    {
        if (_client != null)
        {
            _client.stop();
            _client = null;
        }
    }
    
    /* ------------------------------------------------------------ */
    protected String getBaseUrl()
    {
        return _baseUrl;
    }
    
    /* ------------------------------------------------------------ */
    protected HttpClient getClient()
    {
        return _client;
    }
    
    /* ------------------------------------------------------------ */
    protected Realm getRealm()
    {
        return _realm;
    }
    
    
    /* ------------------------------------------------------------ */
    protected void setRealm(Realm realm)
    {
        _realm = realm;
    }
    
    /* ------------------------------------------------------------ */
    public static void copyStream(InputStream in, OutputStream out)
    {
        try
        {
            byte[] buffer=new byte[1024];
            int len;
            while ((len=in.read(buffer))>=0)
            {
                out.write(buffer,0,len);
            }
        }
        catch (EOFException e)
        {
            System.err.println(e);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
