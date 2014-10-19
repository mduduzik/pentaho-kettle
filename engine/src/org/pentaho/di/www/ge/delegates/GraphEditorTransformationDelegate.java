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

package org.pentaho.di.www.ge.delegates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.trans.TransGraph;

public class GraphEditorTransformationDelegate extends GraphEditorDelegate {
  private static Class<?> PKG = GraphEditor.class; // for i18n purposes, needed by Translator2!!

  /**
   * This contains a map between the name of a transformation and the TransMeta object. If the transformation has no
   * name it will be mapped under a number [1], [2] etc.
   */
  private List<TransMeta> transformationMap;

  /**
   * Remember the debugging configuration per transformation
   */
  private Map<TransMeta, TransDebugMeta> transDebugMetaMap;

  /**
   * Remember the preview configuration per transformation
   */
  private Map<TransMeta, TransDebugMeta> transPreviewMetaMap;

  public GraphEditorTransformationDelegate( GraphEditor ge ) {
    super( ge );
    transformationMap = new ArrayList<TransMeta>();
    transDebugMetaMap = new Hashtable<TransMeta, TransDebugMeta>();
    transPreviewMetaMap = new Hashtable<TransMeta, TransDebugMeta>();
  }

  /**
   * Add a transformation to the
   *
   * @param transMeta
   *          the transformation to add to the map
   * @return true if the transformation was added, false if it couldn't be added (already loaded)
   **/
  public boolean addTransformation( TransMeta transMeta ) {
    int index = transformationMap.indexOf( transMeta );
    if ( index < 0 ) {
      transformationMap.add( transMeta );
      return true;
    } else {
      /*
       * ShowMessageDialog dialog = new ShowMessageDialog(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION,
       * BaseMessages.getString(PKG, "Spoon.Dialog.TransAlreadyLoaded.Title"), "'" + transMeta.toString() + "'" +
       * Const.CR + Const.CR + BaseMessages.getString(PKG, "Spoon.Dialog.TransAlreadyLoaded.Message"));
       * dialog.setTimeOut(6); dialog.open();
       */
      return false;
    }
  }

  public void addTransGraph( TransMeta transMeta ) {
    boolean added = addTransformation( transMeta );
    TransGraph transGraph = new TransGraph( ge, transMeta );
    ge.setActiveTransGraph(transGraph);
  }

  public void executeTransformation( final TransMeta transMeta, final boolean local, final boolean remote,
    final boolean cluster, final boolean preview, final boolean debug, final Date replayDate,
    final boolean safe, LogLevel logLevel ) throws KettleException {

    if ( transMeta == null ) {
      return;
    }

    // See if we need to ask for debugging information...
    //
    TransDebugMeta transDebugMeta = null;
    TransExecutionConfiguration executionConfiguration = null;

    if ( preview ) {
      executionConfiguration = ge.getTransPreviewExecutionConfiguration();
    } else if ( debug ) {
      executionConfiguration = ge.getTransDebugExecutionConfiguration();
    } else {
      executionConfiguration = ge.getTransExecutionConfiguration();
    }

    // Set repository and safe mode information in both the exec config and the metadata
    transMeta.setRepository( ge.getRepository() );
    //TODO: transMeta.setMetaStore( spoon.metaStore );

    executionConfiguration.setRepository( ge.getRepository() );

    executionConfiguration.setSafeModeEnabled( safe );

    if ( debug ) {
      // See if we have debugging information stored somewhere?
      //
      transDebugMeta = transDebugMetaMap.get( transMeta );
      if ( transDebugMeta == null ) {
        transDebugMeta = new TransDebugMeta( transMeta );
        transDebugMetaMap.put( transMeta, transDebugMeta );
      }

      // Set the default number of rows to retrieve on all selected steps...
      //
      List<StepMeta> selectedSteps = transMeta.getSelectedSteps();
      if ( selectedSteps != null && selectedSteps.size() > 0 ) {
        transDebugMeta.getStepDebugMetaMap().clear();
        for ( StepMeta stepMeta : transMeta.getSelectedSteps() ) {
          StepDebugMeta stepDebugMeta = new StepDebugMeta( stepMeta );
          stepDebugMeta.setRowCount( PropsUI.getInstance().getDefaultPreviewSize() );
          stepDebugMeta.setPausingOnBreakPoint( true );
          stepDebugMeta.setReadingFirstRows( false );
          transDebugMeta.getStepDebugMetaMap().put( stepMeta, stepDebugMeta );
        }
      }

    } else if ( preview ) {
      // See if we have preview information stored somewhere?
      //
      transDebugMeta = transPreviewMetaMap.get( transMeta );
      if ( transDebugMeta == null ) {
        transDebugMeta = new TransDebugMeta( transMeta );

        transPreviewMetaMap.put( transMeta, transDebugMeta );
      }

      // Set the default number of preview rows on all selected steps...
      //
      List<StepMeta> selectedSteps = transMeta.getSelectedSteps();
      if ( selectedSteps != null && selectedSteps.size() > 0 ) {
        transDebugMeta.getStepDebugMetaMap().clear();
        for ( StepMeta stepMeta : transMeta.getSelectedSteps() ) {
          StepDebugMeta stepDebugMeta = new StepDebugMeta( stepMeta );
          stepDebugMeta.setRowCount( PropsUI.getInstance().getDefaultPreviewSize() );
          stepDebugMeta.setPausingOnBreakPoint( false );
          stepDebugMeta.setReadingFirstRows( true );
          transDebugMeta.getStepDebugMetaMap().put( stepMeta, stepDebugMeta );
        }
      }
    }

    //Always executing locally to Carte
    executionConfiguration.setExecutingLocally( true );
    executionConfiguration.setExecutingRemotely( false );
    executionConfiguration.setExecutingClustered( false );


    Object[] data = ge.getVariables().getData();
    String[] fields = ge.getVariables().getRowMeta().getFieldNames();
    Map<String, String> variableMap = new HashMap<String, String>();
    variableMap.putAll( executionConfiguration.getVariables() ); // the default
    for ( int idx = 0; idx < fields.length; idx++ ) {
      String value = executionConfiguration.getVariables().get( fields[idx] );
      if ( Const.isEmpty( value ) ) {
        value = data[idx].toString();
      }
      variableMap.put( fields[idx], value );
    }

    executionConfiguration.setVariables( variableMap );
    executionConfiguration.getUsedVariables( transMeta );
    //executionConfiguration.getUsedArguments( transMeta, spoon.getArguments() );
    executionConfiguration.setReplayDate( replayDate );

    executionConfiguration.setLogLevel( logLevel );

/*  TODO:  boolean execConfigAnswer = true;

    if ( debugAnswer == TransDebugDialog.DEBUG_CONFIG && replayDate == null ) {
      TransExecutionConfigurationDialog dialog =
        new TransExecutionConfigurationDialog( spoon.getShell(), executionConfiguration, transMeta );
      execConfigAnswer = dialog.open();
    }

    if ( execConfigAnswer ) {

      ExtensionPointHandler.callExtensionPoint(
        log, KettleExtensionPoint.SpoonTransMetaExecutionStart.id, transMeta );
      ExtensionPointHandler.callExtensionPoint(
        log, KettleExtensionPoint.SpoonTransExecutionConfiguration.id, executionConfiguration );

      // Verify if there is at least one step specified to debug or preview...
      //
      if ( debug || preview ) {
        if ( transDebugMeta.getNrOfUsedSteps() == 0 ) {
          MessageBox box = new MessageBox( spoon.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO );
          box.setText( BaseMessages.getString( PKG, "Spoon.Dialog.Warning.NoPreviewOrDebugSteps.Title" ) );
          box.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.Warning.NoPreviewOrDebugSteps.Message" ) );
          int answer = box.open();
          if ( answer != SWT.YES ) {
            return;
          }
        }
      }
*/
      // addTransLog(transMeta, executionConfiguration.isExecutingLocally());
      // TransLog transLog = spoon.getActiveTransLog();
      //
      TransGraph activeTransGraph = ge.getActiveTransGraph();

      // Is this a local execution?
      //
      if ( executionConfiguration.isExecutingLocally() ) {
        if ( debug || preview ) {
          activeTransGraph.debug( executionConfiguration, transDebugMeta );
        } else {
          //TODO: activeTransGraph.start( executionConfiguration );
        	throw new IllegalArgumentException("Only degbug currently supported");
        }

        // Are we executing remotely?
        //
      } 
      else {
    	  throw new IllegalArgumentException("Only Carte local execution is supported.");
    }
  }
}
