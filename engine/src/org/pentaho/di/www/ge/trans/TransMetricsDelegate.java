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

import java.util.List;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.delegates.GraphEditorDelegate;

public class TransMetricsDelegate extends GraphEditorDelegate {
	  private static Class<?> PKG = GraphEditor.class; // for i18n purposes, needed by Translator2!!

  // private static final LogWriter log = LogWriter.getInstance();

  private TransGraph transGraph;

  /**
   * @param spoon
   * @param transGraph
   */
  public TransMetricsDelegate( GraphEditor ge, TransGraph transGraph ) {
    super( ge );
    this.transGraph = transGraph;
  }

  public void scheduleMetricsDataCollection() {
    // First, see if we need to add the extra view...
    //
 /*   if ( transGraph.extraViewComposite == null || transGraph.extraViewComposite.isDisposed() ) {
      transGraph.addExtraView();
    } else {
      if ( transMetricsTab != null && !transMetricsTab.isDisposed() ) {
        // just set this one active and get out...
        //
        transGraph.extraViewTabFolder.setSelection( transMetricsTab );
        return;
      }
    }

    // Add a transMetricsTab : displays the metrics information in a graphical way...
    //
    transMetricsTab = new CTabItem( transGraph.extraViewTabFolder, SWT.NONE );
    transMetricsTab.setImage( GUIResource.getInstance().getImageGantt() );
    transMetricsTab.setText( BaseMessages.getString( PKG, "Spoon.TransGraph.MetricsTab.Name" ) );

    sMetricsComposite = new ScrolledComposite( transGraph.extraViewTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    sMetricsComposite.setLayout( new FillLayout() );

    // Create a composite, slam everything on there like it was in the history tab.
    //
    metricsComposite = new Composite( sMetricsComposite, SWT.NONE );
    metricsComposite.setBackground( GUIResource.getInstance().getColorBackground() );
    metricsComposite.setLayout( new FormLayout() );

    spoon.props.setLook( metricsComposite );

    setupContent();

    sMetricsComposite.setContent( metricsComposite );
    sMetricsComposite.setExpandHorizontal( true );
    sMetricsComposite.setExpandVertical( true );
    sMetricsComposite.setMinWidth( 800 );
    sMetricsComposite.setMinHeight( 400 );

    transMetricsTab.setControl( sMetricsComposite );

    transGraph.extraViewTabFolder.setSelection( transMetricsTab );

    transGraph.extraViewTabFolder.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( SelectionEvent arg0 ) {
        layoutMetricsComposite();
        updateGraph();
      }
    } );*/
  }

  public void showMetricsView() {
    // What button?
    //
    // XulToolbarButton showLogXulButton =
    // toolbar.getButtonById("trans-show-log");
    // ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();
      scheduleMetricsDataCollection();
  }

  public void updateGraph() {
/*
    transGraph.getDisplay().asyncExec( new Runnable() {
      public void run() {

        if ( metricsComposite != null
          && !metricsComposite.isDisposed() && canvas != null && !canvas.isDisposed() && transMetricsTab != null
          && !transMetricsTab.isDisposed() ) {
          if ( transMetricsTab.isShowing() ) {
            canvas.redraw();
          }
        }
      }
    } );*/
  }

  private long lastRefreshTime;

  public void resetLastRefreshTime() {
    lastRefreshTime = 0L;
  }

}
