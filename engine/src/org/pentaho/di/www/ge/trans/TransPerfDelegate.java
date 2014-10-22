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
import java.util.Map;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.performance.StepPerformanceSnapShot;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.delegates.GraphEditorDelegate;

public class TransPerfDelegate extends GraphEditorDelegate {
  private static Class<?> PKG = GraphEditor.class; // for i18n purposes, needed by Translator2!!

  // private static final LogWriter log = LogWriter.getInstance();

  private static final int DATA_CHOICE_WRITTEN = 0;
  private static final int DATA_CHOICE_READ = 1;
  private static final int DATA_CHOICE_INPUT = 2;
  private static final int DATA_CHOICE_OUTPUT = 3;
  private static final int DATA_CHOICE_UPDATED = 4;
  private static final int DATA_CHOICE_REJECTED = 5;
  private static final int DATA_CHOICE_INPUT_BUFFER_SIZE = 6;
  private static final int DATA_CHOICE_OUTPUT_BUFFER_SIZE = 7;

  private static String[] dataChoices = new String[] {
    BaseMessages.getString( PKG, "StepPerformanceSnapShotDialog.Written" ),
    BaseMessages.getString( PKG, "StepPerformanceSnapShotDialog.Read" ),
    BaseMessages.getString( PKG, "StepPerformanceSnapShotDialog.Input" ),
    BaseMessages.getString( PKG, "StepPerformanceSnapShotDialog.Output" ),
    BaseMessages.getString( PKG, "StepPerformanceSnapShotDialog.Updated" ),
    BaseMessages.getString( PKG, "StepPerformanceSnapShotDialog.Rejected" ),
    BaseMessages.getString( PKG, "StepPerformanceSnapShotDialog.InputBufferSize" ),
    BaseMessages.getString( PKG, "StepPerformanceSnapShotDialog.OutputBufferSize" ), };

  private TransGraph transGraph;


  private Map<String, List<StepPerformanceSnapShot>> stepPerformanceSnapShots;
  private String[] steps;
  private long timeDifference;
  private String title;
  private boolean emptyGraph;

  /**
   * @param spoon
   * @param transGraph
   */
  public TransPerfDelegate( GraphEditor ge, TransGraph transGraph ) {
    super( ge );
    this.transGraph = transGraph;
  }

  public void schedulePerfDataCollection() {
  }

  /**
   * Tell the user that the transformation is not running or that there is no monitoring configured.
   */
  private void showEmptyGraph() {
  }

  public void showPerfView() {

    // What button?
    //
    // XulToolbarButton showLogXulButton = toolbar.getButtonById("trans-show-log");
    // ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();

  }


  /**
   * @return the emptyGraph
   */
  public boolean isEmptyGraph() {
    return emptyGraph;
  }

}
