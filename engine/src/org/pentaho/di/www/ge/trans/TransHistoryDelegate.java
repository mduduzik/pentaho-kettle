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

import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.delegates.GraphEditorDelegate;

public class TransHistoryDelegate  extends GraphEditorDelegate {
  private static Class<?> PKG = GraphEditor.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/trans-history-toolbar.xul";

  private TransGraph transGraph;

  private TransMeta transMeta;

  private enum Mode {
    INITIAL, NEXT_BATCH, ALL
  }

  /**
   * @param spoon
   *          Spoon instance
   * @param transGraph
   *          TransGraph instance
   */
  public TransHistoryDelegate( GraphEditor ge, TransGraph transGraph ) {
    super( ge );
    this.transGraph = transGraph;
  }

  public void scheduleHistoryDataCollection() {
  }

  /**
   * Public for XUL.
   */
  public void clearLogTable() {
  }
}