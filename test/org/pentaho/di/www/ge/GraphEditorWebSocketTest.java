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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.html.dom.HTMLDocumentImpl;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.OptionsBuilder;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.jetty.plus.jaas.JAASLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.CartePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.www.AddTransServlet;
import org.pentaho.di.www.CartePluginInterface;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.GetRootServlet;
import org.pentaho.di.www.GetStatusServlet;
import org.pentaho.di.www.JobMap;
import org.pentaho.di.www.PauseTransServlet;
import org.pentaho.di.www.SlaveServerStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.SocketRepository;
import org.pentaho.di.www.StartTransServlet;
import org.pentaho.di.www.TransformationMap;
import org.pentaho.di.www.WebResult;
import org.pentaho.di.www.ge.trans.TransGridUpdate;
import org.pentaho.di.www.ge.websocket.GERequest;
import org.pentaho.di.www.ge.websocket.GEResponse;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gdata.data.docs.DocumentExportRequestor;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class GraphEditorWebSocketTest {
    private final static Logger logger = LoggerFactory.getLogger(GraphEditorWebSocketTest.class);
    private final static ObjectMapper mapper = new ObjectMapper();

	private CarteSingleton carte = CarteSingleton.getInstance();

	private String carteObjectId;

	private AtmosphereClient wAsyncClient;

	private RequestBuilder requestBuilder;
	private ServletTester tester;
	private int localPort;

	@Before
	public void before() {

		carte.setJobMap(new JobMap());
		carte.setTransformationMap(new TransformationMap());
		carte.setSocketRepository(new SocketRepository(new LogChannel("Carte")));
		
		
	    tester = new ServletTester(false);//socket connection for ws


	    // Add all the servlets defined in kettle-servlets.xml ...
	    //
	    ContextHandlerCollection contexts = new ContextHandlerCollection();
	    HandlerList handlers = new HandlerList();
	    
	    // Root
	    //
	    ServletContextHandler rootHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
	    rootHandler.setContextPath(GetRootServlet.CONTEXT_PATH);
	    handlers.addHandler(rootHandler);
	    GetRootServlet rootServlet = new GetRootServlet();
	    rootServlet.setJettyMode( true );
	    rootHandler.addServlet( new ServletHolder( rootServlet ), "/*" );

		  ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);//new Context( contexts, servlet.getServletPath(), Context.SESSIONS );
		  servletHandler.setContextPath(GetStatusServlet.CONTEXT_PATH);
		  handlers.addHandler(servletHandler);
		  ServletHolder servletHolder = new ServletHolder( GetStatusServlet.class );
		  rootHandler.addServlet( servletHolder, GetStatusServlet.CONTEXT_PATH+"/*" );
		
		  servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);//new Context( contexts, servlet.getServletPath(), Context.SESSIONS );
		  servletHandler.setContextPath(AddTransServlet.CONTEXT_PATH);
		  handlers.addHandler(servletHandler);
		  servletHolder = new ServletHolder( AddTransServlet.class );
		  rootHandler.addServlet( servletHolder, AddTransServlet.CONTEXT_PATH);
		
		  servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);//new Context( contexts, servlet.getServletPath(), Context.SESSIONS );
		  servletHandler.setContextPath(StartTransServlet.CONTEXT_PATH);
		  handlers.addHandler(servletHandler);
		  servletHolder = new ServletHolder( StartTransServlet.class );
		  rootHandler.addServlet( servletHolder, StartTransServlet.CONTEXT_PATH+"/*" );
		  
		  servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);//new Context( contexts, servlet.getServletPath(), Context.SESSIONS );
		  servletHandler.setContextPath(PauseTransServlet.CONTEXT_PATH);
		  handlers.addHandler(servletHandler);
		  servletHolder = new ServletHolder( PauseTransServlet.class );
		  rootHandler.addServlet( servletHolder, PauseTransServlet.CONTEXT_PATH+"/*" );
	      
	    // Atmosphere 
		AtmosphereServlet atmosphereServlet = new AtmosphereServlet();
	    servletHolder = new ServletHolder(atmosphereServlet);
	    servletHolder.setInitParameter("com.sun.jersey.config.property.packages","org.pentaho.di.www.websocket");
	    //holder.setInitParameter("org.atmosphere.cpr.packages", "org.pentaho.di.www.ge.websocket");
	    servletHolder.setInitParameter("org.atmosphere.websocket.messageContentType", "application/json");
	    servletHolder.setAsyncSupported(true);
	    servletHolder.setInitParameter("org.atmosphere.useWebSocket","true");
	    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	    context.addServlet(servletHolder, "/websockets/*");
	    handlers.addHandler(context);

	    // add all handlers/contexts to server
	    tester.getServer().setHandler(handlers);

		try {
			tester.getServer().start();
			System.out.println("Started");
			
			carteObjectId = addTransServlet();
			System.out.println(String.format("Installed trans %s",
					carteObjectId));
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
		
		wAsyncClient = ClientFactory.getDefault().newClient(AtmosphereClient.class);
        requestBuilder = wAsyncClient.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri("http://127.0.0.1:"+localPort+ "/ged/tenant1000")
                .trackMessageLength(true)
                .encoder(new Encoder<GERequest, String>() {
                    @Override
                    public String encode(GERequest data) {
                        try {
                            return mapper.writeValueAsString(data);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .decoder(new Decoder<String, GERequest>() {
                    @Override
                    public GERequest decode(Event type, String data) {

                        data = data.trim();

                        // Padding
                        if (data.length() == 0) {
                            return null;
                        }

                        if (type.equals(Event.MESSAGE)) {
                            try {
                                return mapper.readValue(data, GERequest.class);
                            } catch (IOException e) {
                                logger.debug("Invalid message {}", data);
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                })
                .encoder(new Encoder<GEResponse, String>() {
                    @Override
                    public String encode(GEResponse data) {
                        try {
                            return mapper.writeValueAsString(data);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .decoder(new Decoder<String, GEResponse>() {
                    @Override
                    public GEResponse decode(Event type, String data) {

                        data = data.trim();

                        // Padding
                        if (data.length() == 0) {
                            return null;
                        }

                        if (type.equals(Event.MESSAGE)) {
                            try {
                                return mapper.readValue(data, GEResponse.class);
                            } catch (IOException e) {
                                logger.debug("Invalid message {}", data);
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                })
                .encoder(new Encoder<TransGridUpdate, String>() {
                    @Override
                    public String encode(TransGridUpdate data) {
                        try {
                            return mapper.writeValueAsString(data);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .decoder(new Decoder<String, TransGridUpdate>() {
                    @Override
                    public TransGridUpdate decode(Event type, String data) {

                        data = data.trim();

                        // Padding
                        if (data.length() == 0) {
                            return null;
                        }

                        if (type.equals(Event.MESSAGE)) {
                            try {
                                return mapper.readValue(data, TransGridUpdate.class);
                            } catch (IOException e) {
                                logger.debug("Invalid message {}", data);
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET)
                .transport(Request.TRANSPORT.SSE)
                .transport(Request.TRANSPORT.LONG_POLLING);
	}

	@After
	public void after() {
		try {
			tester.stop();
			carte.getDetections().clear();
			carte.getSocketRepository().closeAll();
			System.out.println("Stopped");
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}
	
	@Test
	public void testClient() throws IOException  {
		Socket socket = wAsyncClient.create();
        socket.on("message", new Function<GEResponse>() {
            @Override
            public void on(GEResponse t) {
                logger.info("Message: "+t.toString());
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

/*        logger.info("Choose Name: ");
        String name = null;
        String a = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (!(a.equals("quit"))) {
            a = br.readLine();
            if (name == null) {
                name = a;
            }
            socket.fire(new Message(name, a));
        }*/
        socket.close();
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
	public static Trans generateTestTransformation() {
		RowGeneratorMeta A = new RowGeneratorMeta();
		A.allocate(3);
		A.setRowLimit("10000000");

		A.getFieldName()[0] = "ID";
		A.getFieldType()[0] = ValueMeta
				.getTypeDesc(ValueMetaInterface.TYPE_INTEGER);
		A.getFieldLength()[0] = 7;
		A.getValue()[0] = "1234";

		A.getFieldName()[1] = "Name";
		A.getFieldType()[1] = ValueMeta
				.getTypeDesc(ValueMetaInterface.TYPE_STRING);
		A.getFieldLength()[1] = 35;
		A.getValue()[1] = "Some name";

		A.getFieldName()[2] = "Last updated";
		A.getFieldType()[2] = ValueMeta
				.getTypeDesc(ValueMetaInterface.TYPE_DATE);
		A.getFieldFormat()[2] = "yyyy/MM/dd";
		A.getValue()[2] = "2010/02/09";

		TransMeta transMeta = TransPreviewFactory
				.generatePreviewTransformation(null, A, "A");
		transMeta.setName("CarteUnitTest");
		transMeta.setSizeRowset(2500);
		transMeta.setFeedbackSize(50000);
		transMeta.setUsingThreadPriorityManagment(false);

		return new Trans(transMeta);
	}

	public String addTransServlet() {
		String carteObjId = null;

		HttpTester request = new HttpTester();
		HttpTester response = new HttpTester();
		request.setMethod("GET");
		request.setHeader("Host", "tester");
		request.setURI(AddTransServlet.CONTEXT_PATH + "?xml=Y");
		request.setVersion("HTTP/1.0");
		try {

			TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
			Trans trans = GraphEditorWebSocketTest.generateTestTransformation();
			TransConfiguration transConfig = new TransConfiguration(
					trans.getTransMeta(), transExecConfig);
			request.setContent(transConfig.getXML());
			response.parse(tester.getResponses(request.generate()));

			WebResult webResult = new WebResult( XMLHandler.loadXMLString( response.getContent(), WebResult.XML_TAG ) );
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

	public SlaveServerStatus getStatus() {
		HttpTester request = new HttpTester();
		HttpTester response = new HttpTester();
		request.setMethod("GET");
		request.setHeader("Host", "tester");
		request.setURI(GetStatusServlet.CONTEXT_PATH + "?xml=Y");
		request.setVersion("HTTP/1.0");
		try {
			response.parse(tester.getResponses(request.generate()));
			return SlaveServerStatus.fromXML(response.getContent());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
