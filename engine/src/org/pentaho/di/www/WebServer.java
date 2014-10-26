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

package org.pentaho.di.www;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.Servlet;

import org.atmosphere.cpr.AtmosphereServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.plus.jaas.JAASLoginService;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Credential;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.CartePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.i18n.BaseMessages;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class WebServer {
  private static Class<?> PKG = WebServer.class; // for i18n purposes, needed by Translator2!!

  private LogChannelInterface log;

  public static final int PORT = 80;

  private Server server;

  private TransformationMap transformationMap;
  private JobMap jobMap;
  private List<SlaveServerDetection> detections;
  private SocketRepository socketRepository;

  private String hostname;
  private int port;

  private Timer slaveMonitoringTimer;

  private String passwordFile;

  public WebServer( LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap,
    SocketRepository socketRepository, List<SlaveServerDetection> detections, String hostname, int port,
    boolean join, String passwordFile ) throws Exception {
    this.log = log;
    this.transformationMap = transformationMap;
    this.jobMap = jobMap;
    this.socketRepository = socketRepository;
    this.detections = detections;
    this.hostname = hostname;
    this.port = port;
    this.passwordFile = passwordFile;

    startServer();

    // Start the monitoring of the registered slave servers...
    //
    startSlaveMonitoring();

    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.CarteStartup.id, this );
    } catch ( KettleException e ) {
      // Log error but continue regular operations to make sure Carte continues to run properly
      //
      log.logError( "Error calling extension point CarteStartup", e );
    }

    if ( join ) {
      server.join();
    }
  }

  public WebServer( LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap,
    SocketRepository socketRepository, List<SlaveServerDetection> slaveServers, String hostname, int port ) throws Exception {
    this( log, transformationMap, jobMap, socketRepository, slaveServers, hostname, port, true );
  }

  public WebServer( LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap,
    SocketRepository socketRepository, List<SlaveServerDetection> detections, String hostname, int port,
    boolean join ) throws Exception {
    this( log, transformationMap, jobMap, socketRepository, detections, hostname, port, join, null );
  }

  public Server getServer() {
    return server;
  }

  public void startServer() throws Exception {
    server = new Server();

    Constraint constraint = new Constraint();
    constraint.setName( Constraint.__BASIC_AUTH );
    constraint.setRoles( new String[] { Constraint.ANY_ROLE } );
    constraint.setAuthenticate( true );

    ConstraintMapping constraintMapping = new ConstraintMapping();
    constraintMapping.setConstraint( constraint );
    constraintMapping.setPathSpec( "/*" );

    // Set up the security handler, optionally with JAAS
    //
    //SecurityHandler securityHandler = new SecurityHandler();
    ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();

    if ( System.getProperty( "loginmodulename" ) != null
      && System.getProperty( "java.security.auth.login.config" ) != null ) {
      JAASLoginService jaasRealm = new JAASLoginService( "Kettle" );
      jaasRealm.setLoginModuleName( System.getProperty( "loginmodulename" ) );
      securityHandler.setLoginService(jaasRealm);
    } else {
      HashLoginService hashUserRealm;
      SlaveServer slaveServer = transformationMap.getSlaveServerConfig().getSlaveServer();
      if ( !Const.isEmpty( slaveServer.getPassword() ) ) {
        hashUserRealm = new HashLoginService( "Kettle" );
        hashUserRealm.putUser(slaveServer.getUsername(),Credential.getCredential(slaveServer.getPassword() ),new String[]{slaveServer.getUsername()});
      } else {
        // See if there is a kettle.pwd file in the KETTLE_HOME directory:
        if ( Const.isEmpty( passwordFile ) ) {
          File homePwdFile = new File( Const.getKettleCartePasswordFile() );
          if ( homePwdFile.exists() ) {
            passwordFile = Const.getKettleCartePasswordFile();
          } else {
            passwordFile = Const.getKettleLocalCartePasswordFile();
          }
        }
        hashUserRealm = new HashLoginService( "Kettle", passwordFile );
      }
      securityHandler.setLoginService(hashUserRealm);
    }

    securityHandler.setConstraintMappings( new ConstraintMapping[] { constraintMapping } );

    // Add all the servlets defined in kettle-servlets.xml ...
    //
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    HandlerList handlers = new HandlerList();
    handlers.addHandler(securityHandler);
    
    // Root
    //
    ServletContextHandler rootHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    rootHandler.setContextPath(GetRootServlet.CONTEXT_PATH);
    handlers.addHandler(rootHandler);
    GetRootServlet rootServlet = new GetRootServlet();
    rootServlet.setJettyMode( true );
    rootHandler.addServlet( new ServletHolder( rootServlet ), "/*" );

    PluginRegistry pluginRegistry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = pluginRegistry.getPlugins( CartePluginType.class );
    for ( PluginInterface plugin : plugins ) {

      CartePluginInterface servlet = (CartePluginInterface) pluginRegistry.loadClass( plugin );
      servlet.setup( transformationMap, jobMap, socketRepository, detections );
      servlet.setJettyMode( true );

      ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);//new Context( contexts, servlet.getServletPath(), Context.SESSIONS );
      servletHandler.setContextPath(servlet.getContextPath());
      handlers.addHandler(servletHandler);
      ServletHolder servletHolder = new ServletHolder( (Servlet) servlet );
      rootHandler.addServlet( servletHolder, servlet.getContextPath()+"/*" );
    }

    // setup jersey (REST)
/*    ServletHolder jerseyServletHolder = new ServletHolder( ServletContainer.class );
    jerseyServletHolder.setInitParameter(
      "com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig" );
    jerseyServletHolder.setInitParameter( "com.sun.jersey.config.property.packages", "org.pentaho.di.www.jaxrs" );
    rootHandler.addServlet( jerseyServletHolder, "/api/*" );*/

    
    // Atmosphere 
	AtmosphereServlet atmosphereServlet = new AtmosphereServlet();
    ServletHolder atmosphereServletHolder = new ServletHolder(atmosphereServlet);
    atmosphereServletHolder.setInitParameter("com.sun.jersey.config.property.packages","org.pentaho.di.www.websocket");
    atmosphereServletHolder.setAsyncSupported(true);
    atmosphereServletHolder.setInitParameter("org.atmosphere.useWebSocket","true");
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/ged/*");
    rootHandler.addServlet(atmosphereServletHolder, "/ged/*");
    //handlers.addHandler(context);

    // Allow png files to be shown for transformations and jobs...
    //
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setResourceBase( "temp" );
    handlers.addHandler(resourceHandler);

    // add all handlers/contexts to server
    server.setHandler(handlers);

    // Start execution
    createListeners();

    server.start();
  }

  public void join() throws InterruptedException {
    server.join();
  }

  public void stopServer() {

    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.CarteShutdown.id, this );
    } catch ( KettleException e ) {
      // Log error but continue regular operations to make sure Carte can be shut down properly.
      //
      log.logError( "Error calling extension point CarteStartup", e );
    }

    try {
      if ( server != null ) {

        // Stop the monitoring timer
        //
        if ( slaveMonitoringTimer != null ) {
          slaveMonitoringTimer.cancel();
          slaveMonitoringTimer = null;
        }

        // Clean up all the server sockets...
        //
        socketRepository.closeAll();

        // Stop the server...
        //
        server.stop();
      }
    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "WebServer.Error.FailedToStop.Title" ), BaseMessages.getString(
        PKG, "WebServer.Error.FailedToStop.Msg", "" + e ) );
    }
  }

  private void createListeners() {
    SelectChannelConnector httpConnector = new SelectChannelConnector();
    httpConnector.setPort( port );
    httpConnector.setHost( hostname );
    server.setConnectors(new Connector[] { httpConnector });
    httpConnector.setName( BaseMessages.getString( PKG, "WebServer.Log.KettleHTTPListener", hostname ) );
    log.logBasic( BaseMessages.getString( PKG, "WebServer.Log.CreateListener", hostname, "" + port ) );
    server.setConnectors( new Connector[] { httpConnector } );
	    
/*    SocketConnector connector = new SocketConnector();
    connector.setPort( port );
    connector.setHost( hostname );
    connector.setName( BaseMessages.getString( PKG, "WebServer.Log.KettleHTTPListener", hostname ) );
    log.logBasic( BaseMessages.getString( PKG, "WebServer.Log.CreateListener", hostname, "" + port ) );
    server.setConnectors( new Connector[] { connector } );*/
  }

  /**
   * @return the hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @param hostname
   *          the hostname to set
   */
  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  /**
   * @return the slave server detections
   */
  public List<SlaveServerDetection> getDetections() {
    return detections;
  }

  /**
   * This method registers a timer to check up on all the registered slave servers every X seconds.<br>
   */
  private void startSlaveMonitoring() {
    slaveMonitoringTimer = new Timer( "WebServer Timer" );
    TimerTask timerTask = new TimerTask() {

      public void run() {
        for ( SlaveServerDetection slaveServerDetection : detections ) {
          SlaveServer slaveServer = slaveServerDetection.getSlaveServer();

          // See if we can get a status...
          //
          try {
            // TODO: consider making this lighter or retaining more information...
            slaveServer.getStatus(); // throws the exception
            slaveServerDetection.setActive( true );
            slaveServerDetection.setLastActiveDate( new Date() );
          } catch ( Exception e ) {
            slaveServerDetection.setActive( false );
            slaveServerDetection.setLastInactiveDate( new Date() );

            // TODO: kick it out after a configurable period of time...
          }
        }
      }
    };
    slaveMonitoringTimer.schedule( timerTask, 20000, 20000 );
  }

  /**
   * @return the socketRepository
   */
  public SocketRepository getSocketRepository() {
    return socketRepository;
  }

  /**
   * @param socketRepository
   *          the socketRepository to set
   */
  public void setSocketRepository( SocketRepository socketRepository ) {
    this.socketRepository = socketRepository;
  }

  public String getPasswordFile() {
    return passwordFile;
  }

  public void setPasswordFile( String passwordFile ) {
    this.passwordFile = passwordFile;
  }

  public LogChannelInterface getLog() {
    return log;
  }

  public void setLog( LogChannelInterface log ) {
    this.log = log;
  }

  public TransformationMap getTransformationMap() {
    return transformationMap;
  }

  public void setTransformationMap( TransformationMap transformationMap ) {
    this.transformationMap = transformationMap;
  }

  public JobMap getJobMap() {
    return jobMap;
  }

  public void setJobMap( JobMap jobMap ) {
    this.jobMap = jobMap;
  }

  public int getPort() {
    return port;
  }

  public void setPort( int port ) {
    this.port = port;
  }

  public Timer getSlaveMonitoringTimer() {
    return slaveMonitoringTimer;
  }

  public void setSlaveMonitoringTimer( Timer slaveMonitoringTimer ) {
    this.slaveMonitoringTimer = slaveMonitoringTimer;
  }

  public void setServer( Server server ) {
    this.server = server;
  }

  public void setDetections( List<SlaveServerDetection> detections ) {
    this.detections = detections;
  }
}
