package org.pentaho.di.www.ge.delegates;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.www.ge.GraphEditor;

public abstract class GraphEditorDelegate {
  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "GraphEditor (delegate)", LoggingObjectType.SPOON, null );

  protected GraphEditor ge;
  protected LogChannelInterface log;

  protected GraphEditorDelegate( GraphEditor ge ) {
    this.ge = ge;
    this.log = ge.getLog();
  }
}
