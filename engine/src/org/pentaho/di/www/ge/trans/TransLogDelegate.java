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

package org.pentaho.di.www.ge.trans;

import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.delegates.GraphEditorDelegate;

public class TransLogDelegate   extends GraphEditorDelegate {
  private static Class<?> PKG = GraphEditor.class; // for i18n purposes, needed by Translator2!!

  private static final String XUL_FILE_TRANS_LOG_TOOLBAR = "ui/trans-log-toolbar.xul";

  private TransGraph transGraph;

  private LogBrowser logBrowser;

  /**
   * @param spoon
   */
  public TransLogDelegate( GraphEditor ge, TransGraph transGraph ) {
    super( ge );
    this.transGraph = transGraph;
  }

  public void scheduleLogDataCollection() {
/*
    logBrowser = new LogBrowser( transGraph );
    logBrowser.installLogSniffer();

    // If the transformation is closed, we should dispose of all the logging information in the buffer and registry for
    // this transformation
    //
    transGraph.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        if ( transGraph.trans != null ) {
          KettleLogStore.discardLines( transGraph.trans.getLogChannelId(), true );
        }
      }
    } );

    transLogTab.setControl( transLogComposite );

    transGraph.extraViewTabFolder.setSelection( transLogTab );*/
  }

  public void showLogView() {

    // What button?
    //
    // XulToolbarButton showLogXulButton = toolbar.getButtonById("trans-show-log");
    // ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();
    scheduleLogDataCollection();

    // spoon.addTransLog(transMeta);
  }

  public void showLogSettings() {
    //spoon.setLog();
  }

  public void clearLog() {
/*    if ( transLogText != null && !transLogText.isDisposed() ) {
      transLogText.setText( "" );
    }
    Map<StepMeta, String> stepLogMap = transGraph.getStepLogMap();
    if ( stepLogMap != null ) {
      stepLogMap.clear();
      transGraph.getDisplay().asyncExec( new Runnable() {
        public void run() {
          transGraph.redraw();
        }
      } );
    }*/
  }

  public void showErrors() {
  }

  public LogBrowser getLogBrowser() {
    return logBrowser;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getData()
   */
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  public String getName() {
    return "translog";
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
   */
  public void setData( Object data ) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
   */
  public void setName( String name ) {
    // TODO Auto-generated method stub

  }

}
