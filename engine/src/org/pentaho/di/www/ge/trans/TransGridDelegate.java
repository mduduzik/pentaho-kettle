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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.atmosphere.cpr.BroadcasterFactory;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.delegates.GraphEditorDelegate;
import org.pentaho.di.www.ge.websocket.message.trans.GETransGridUpdate;
import org.pentaho.di.www.ge.websocket.message.trans.GETransGridUpdateEncoderDecoder;

public class TransGridDelegate extends GraphEditorDelegate {
	private static Class<?> PKG = GraphEditor.class; // for i18n purposes,
														// needed by
														// Translator2!!

	private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/trans-grid-toolbar.xul";

	public static final long REFRESH_TIME = 50L;

	public static final long UPDATE_TIME_VIEW = 50L;

	private TransGraph transGraph;

	private boolean refresh_busy;

	private long lastUpdateView;

	private boolean hideInactiveSteps;

	private boolean showSelectedSteps;

	private ScheduledFuture<?> gridRefreshTimerFuture;
	private Map<String,Object[]> currentStepMap;

	/**
	 * @param spoon
	 * @param transGraph
	 */
	public TransGridDelegate(GraphEditor ge, TransGraph transGraph) {
		super(ge);
		this.transGraph = transGraph;

		hideInactiveSteps = false;
	}

	public void showGridView() {
		scheduleGridDataCollection();
	}

	public RowMetaInterface createResultRowMetaInterface(
			ValueMetaInterface[] valuesMeta) {
		RowMetaInterface rm = new RowMeta();

		for (int i = 0; i < valuesMeta.length; i++) {
			rm.addValueMeta(valuesMeta[i]);
		}

		return rm;
	}

	/**
	 * Add a grid with the execution metrics per step in a table view
	 *
	 */
	public void scheduleGridDataCollection() {
		// Add a timer to update this view every couple of seconds...
		//
		final AtomicBoolean busy = new AtomicBoolean(false);
	    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	    gridRefreshTimerFuture = scheduler.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						log.logDebug(String.format("Running GridDelegate refreshView()"));
						busy.set(true);
						refreshView();
						busy.set(false);
					}
				}, 0, REFRESH_TIME, TimeUnit.MILLISECONDS);
	}

	private void refreshView() {
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
		ValueMetaInterface[] valuesMeta = {
				new ValueMeta("Stepname", ValueMeta.TYPE_STRING),
				new ValueMeta("Read", ValueMeta.TYPE_STRING),
				new ValueMeta("Copynr", ValueMeta.TYPE_STRING),
				new ValueMeta("Read", ValueMeta.TYPE_STRING),
				new ValueMeta("Written", ValueMeta.TYPE_STRING),
				new ValueMeta("Input", ValueMeta.TYPE_STRING),
				new ValueMeta("Output", ValueMeta.TYPE_STRING),
				new ValueMeta("Updated", ValueMeta.TYPE_STRING),
				new ValueMeta("Rejected", ValueMeta.TYPE_STRING),
				new ValueMeta("Errors", ValueMeta.TYPE_STRING),
				new ValueMeta("Active", ValueMeta.TYPE_STRING),
				new ValueMeta("Time", ValueMeta.TYPE_STRING),
				new ValueMeta("Speed", ValueMeta.TYPE_STRING) };

		RowMetaInterface rm = createResultRowMetaInterface(valuesMeta);

		boolean insert = true;
		int nrSteps = -1;
		int totalSteps = -1;

		if (refresh_busy) {
			return;
		}

		refresh_busy = true;

		boolean broadCastUpdate = false;
		long time = new Date().getTime();
		long msSinceLastUpdate = time - lastUpdateView;
		if (transGraph.trans != null && !transGraph.trans.isPreparing()
				/*&& msSinceLastUpdate > UPDATE_TIME_VIEW*/) {
			lastUpdateView = time;

			nrSteps = transGraph.trans.nrSteps();
			totalSteps = nrSteps;
			if (hideInactiveSteps) {
				nrSteps = transGraph.trans.nrActiveSteps();
			}

			StepExecutionStatus[] stepStatusLookup = transGraph.trans
					.getTransStepExecutionStatusLookup();
			boolean[] isRunningLookup = transGraph.trans
					.getTransStepIsRunningLookup();

			int nr = 0;

			for (int i = 0; i < totalSteps; i++) {
				StepInterface baseStep = transGraph.trans.getRunThread(i);
				StepStatus stepStatus = new StepStatus(baseStep);

				String[] fields = stepStatus.getTransLogFields();

				// Anti-flicker: if nothing has changed, don't change it on the
				// screen!
				Object[] row = new Object[fields.length];
				for (int f = 1; f < fields.length; f++) {
					if (f == 4)
						continue;
					else if (f > 4)
						row[f-1] = fields[f-1];
					else
						row[f-1] = fields[f];
				}
				
				list.add(new RowMetaAndData(rm, row));
				if (currentStepMap == null) {//First update
					currentStepMap = new HashMap<String, Object[]>();
					currentStepMap.put(baseStep.getStepname(),row);
					broadCastUpdate = true;
				}
				else {
					Object[] row_ = currentStepMap.get(baseStep.getStepname());
					if (row_ != null) {
						if (!Arrays.equals(row, row_)) {
							broadCastUpdate = true;
							currentStepMap.put(baseStep.getStepname(),row);
						}
					}
				}
				nr++;
			}
		}
		
		//Push update
		if (broadCastUpdate && !list.isEmpty())
			ge.broadcast(this.getClass().getSimpleName(), GETransGridUpdateEncoderDecoder.INSTANCE.encode(new GETransGridUpdate(list)));

		refresh_busy = false;
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
		return "transgrid";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
	 */
	public void setData(Object data) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
	 */
	public void setName(String name) {
		// TODO Auto-generated method stub
	}

	public void stopGridDataCollection() {
		gridRefreshTimerFuture.cancel(false);
		refreshView();
	}
}
