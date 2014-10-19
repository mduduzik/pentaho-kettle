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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.delegates.GraphEditorDelegate;


public class TransPreviewDelegate  extends GraphEditorDelegate {
	  private static Class<?> PKG = GraphEditor.class; // for i18n purposes, needed by Translator2!!

  private static final String XUL_FILE_TRANS_PREVIEW_TOOLBAR = "ui/trans-preview-toolbar.xul";

  private TransGraph transGraph;

  protected Map<StepMeta, RowMetaInterface> previewMetaMap;
  protected Map<StepMeta, List<Object[]>> previewDataMap;
  protected Map<StepMeta, StringBuffer> previewLogMap;

  public enum PreviewMode {
    FIRST, LAST, OFF,
  }

  private PreviewMode previewMode;

  private StepMeta selectedStep;
  protected StepMeta lastSelectedStep;

  /**
   * @param spoon
   * @param transGraph
   */
  public TransPreviewDelegate( GraphEditor ge, TransGraph transGraph ) {
    super( ge );
    this.transGraph = transGraph;

    previewMetaMap = new HashMap<StepMeta, RowMetaInterface>();
    previewDataMap = new HashMap<StepMeta, List<Object[]>>();
    previewLogMap = new HashMap<StepMeta, StringBuffer>();

    previewMode = PreviewMode.FIRST;
  }

  public void showPreviewView() {
      addTransPreview();
      //transGraph.checkEmptyExtraView();
  }

  /**
   * Add a grid with the execution metrics per step in a table view
   *
   */
  public void addTransPreview() {
  }


  /**
   * This refresh is driven by outside influenced using listeners and so on.
   */
  public synchronized void refreshView() {
    // Which step do we preview...
    //
    StepMeta stepMeta = selectedStep; // copy to prevent race conditions and so on.
    if ( stepMeta == null ) {
      return;
    } else {
      lastSelectedStep = selectedStep;
    }

    // Do we have a log for this selected step?
    // This means the preview work is still running or it error-ed out.
    //
    boolean errorStep = false;
    if ( transGraph.trans != null ) {
      List<StepInterface> steps = transGraph.trans.findBaseSteps( stepMeta.getName() );
      if ( steps != null && steps.size() > 0 ) {
        errorStep = steps.get( 0 ).getErrors() > 0;
      }
    }

    StringBuffer logText = previewLogMap.get( stepMeta );
    if ( errorStep && logText != null && logText.length() > 0 ) {
      showLogText( stepMeta, logText.toString() );
      return;
    }

    // If the preview work is done we have row meta-data and data for each step.
    //
    RowMetaInterface rowMeta = previewMetaMap.get( stepMeta );
    if ( rowMeta != null ) {
      List<Object[]> rowData = previewDataMap.get( stepMeta );

      try {
        showPreviewGrid( null/*transGraph.getManagedObject()*/, stepMeta, rowMeta, rowData );
      } catch ( Exception e ) {
        e.printStackTrace();
        logText.append( Const.getStackTracker( e ) );
        showLogText( stepMeta, logText.toString() );
      }
    }
  }

  protected void showPreviewGrid( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface rowMeta,
    List<Object[]> rowsData ) throws KettleException {
  }

  protected void showLogText( StepMeta stepMeta, String loggingText ) {
    clearPreviewComposite();
  }

  private void clearPreviewComposite() {
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
    return "transpreview";
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



  /**
   * @return the active
   */
  public boolean isActive() {
    return previewMode != PreviewMode.OFF;
  }

  public void setPreviewMode( PreviewMode previewMode ) {
    this.previewMode = previewMode;
  }

  public void capturePreviewData( final Trans trans, List<StepMeta> stepMetas ) {
    final StringBuffer loggingText = new StringBuffer();

    // First clean out previous preview data. Otherwise this method leaks memory like crazy.
    //
    previewLogMap.clear();
    previewMetaMap.clear();
    previewDataMap.clear();

    try {
      final TransMeta transMeta = trans.getTransMeta();

      for ( final StepMeta stepMeta : stepMetas ) {

        final RowMetaInterface rowMeta = transMeta.getStepFields( stepMeta ).clone();
        previewMetaMap.put( stepMeta, rowMeta );
        final List<Object[]> rowsData;
        if ( previewMode == PreviewMode.LAST ) {
          rowsData = new LinkedList<Object[]>();
        } else {
          rowsData = new ArrayList<Object[]>();
        }

        previewDataMap.put( stepMeta, rowsData );
        previewLogMap.put( stepMeta, loggingText );

        StepInterface step = trans.findRunThread( stepMeta.getName() );

        if ( step != null ) {

          switch ( previewMode ) {
            case LAST:
              step.addRowListener( new RowAdapter() {
                @Override
                public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
                  try {
                    rowsData.add( rowMeta.cloneRow( row ) );
                    if ( rowsData.size() > ge.getDefaultPreviewSize() ) {
                      rowsData.remove( 0 );
                    }
                  } catch ( Exception e ) {
                    throw new KettleStepException( "Unable to clone row for metadata : " + rowMeta, e );
                  }
                }
              } );
              break;
            default:
              step.addRowListener( new RowAdapter() {

                @Override
                public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
                  if ( rowsData.size() < ge.getDefaultPreviewSize() ) {
                    try {
                      rowsData.add( rowMeta.cloneRow( row ) );
                    } catch ( Exception e ) {
                      throw new KettleStepException( "Unable to clone row for metadata : " + rowMeta, e );
                    }
                  }
                }
              } );
              break;
          }
        }

      }
    } catch ( Exception e ) {
      loggingText.append( Const.getStackTracker( e ) );
    }

    // In case there were errors during preview...
    //
    trans.addTransListener( new TransAdapter() {
      @Override
      public void transFinished( Trans trans ) throws KettleException {
        // Copy over the data from the previewDelegate...
        //
        if ( trans.getErrors() != 0 ) {
          // capture logging and store it...
          //
          for ( StepMetaDataCombi combi : trans.getSteps() ) {
            if ( combi.copy == 0 ) {
              StringBuffer logBuffer =
                KettleLogStore.getAppender().getBuffer( combi.step.getLogChannel().getLogChannelId(), false );
              previewLogMap.put( combi.stepMeta, logBuffer );
            }
          }
        }
      }
    } );
  }

  public void addPreviewData( StepMeta stepMeta, RowMetaInterface rowMeta, List<Object[]> rowsData,
    StringBuffer buffer ) {
    previewLogMap.put( stepMeta, buffer );
    previewMetaMap.put( stepMeta, rowMeta );
    previewDataMap.put( stepMeta, rowsData );
  }

  /**
   * @return the selectedStep
   */
  public StepMeta getSelectedStep() {
    return selectedStep;
  }

  /**
   * @param selectedStep
   *          the selectedStep to set
   */
  public void setSelectedStep( StepMeta selectedStep ) {
    this.selectedStep = selectedStep;
  }

  public PreviewMode getPreviewMode() {
    return previewMode;
  }

  public void first() {
    previewMode = PreviewMode.FIRST;
  }

  public void last() {
    previewMode = PreviewMode.LAST;
  }

  public void off() {
    previewMode = PreviewMode.OFF;
  }
}
