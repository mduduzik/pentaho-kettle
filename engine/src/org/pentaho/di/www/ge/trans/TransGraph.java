//CHECKSTYLE:FileLength:OFF
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.atmosphere.cpr.AtmosphereResource;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogParentProvidedInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.debug.BreakPointListener;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.job.JobGraph;
import org.pentaho.di.www.ge.websocket.message.GEFinishedResponse;
import org.pentaho.di.www.ge.websocket.message.GEFinishedResponseEncoderDecoder;
import org.pentaho.di.www.ge.websocket.message.GEUpdateEncoderDecoder;

import sun.print.resources.serviceui;

/**
 * This class handles the display of the transformations in a graphical way
 * using icons, arrows, etc. One transformation is handled per TransGraph
 *
 * @author Matt
 * @since 17-mei-2003
 *
 */
public class TransGraph implements LogParentProvidedInterface {
	private static Class<?> PKG = GraphEditor.class; // for i18n purposes,
														// needed by
														// Translator2!!

	private LogChannelInterface log;

	private static final int HOP_SEL_MARGIN = 9;

	private static final String XUL_FILE_TRANS_TOOLBAR = "ui/trans-toolbar.xul";

	public static final String START_TEXT = BaseMessages.getString(PKG,
			"TransLog.Button.StartTransformation");

	public static final String PAUSE_TEXT = BaseMessages.getString(PKG,
			"TransLog.Button.PauseTransformation");

	public static final String RESUME_TEXT = BaseMessages.getString(PKG,
			"TransLog.Button.ResumeTransformation");

	public static final String STOP_TEXT = BaseMessages.getString(PKG,
			"TransLog.Button.StopTransformation");

	private TransMeta transMeta;

	private int iconsize;

	private Point lastclick;

	private Point lastMove;

	private Point[] previous_step_locations;

	private Point[] previous_note_locations;

	private List<StepMeta> selectedSteps;

	private StepMeta selectedStep;

	private List<StepMeta> mouseOverSteps;

	private List<NotePadMeta> selectedNotes;

	private NotePadMeta selectedNote;

	private TransHopMeta candidate;

	private Point drop_candidate;

	private GraphEditor ge;

	// public boolean shift, control;

	private boolean split_hop;

	private TransHopMeta last_hop_split;

	/**
	 * A list of remarks on the current Transformation...
	 */
	private List<CheckResultInterface> remarks;

	/**
	 * A list of impacts of the current transformation on the used databases.
	 */
	private List<DatabaseImpact> impact;

	/**
	 * Indicates whether or not an impact analysis has already run.
	 */
	private boolean impactFinished;

	private TransDebugMeta lastTransDebugMeta;

	protected NotePadMeta ni = null;

	protected TransHopMeta currentHop;

	protected StepMeta currentStep;

	private List<AreaOwner> areaOwners;

	private boolean initialized;

	private boolean running;

	private boolean halted;

	private boolean halting;

	private boolean debug;

	private boolean pausing;

	public TransLogDelegate transLogDelegate;

	public TransGridDelegate transGridDelegate;

	public TransHistoryDelegate transHistoryDelegate;

	public TransPerfDelegate transPerfDelegate;

	public TransMetricsDelegate transMetricsDelegate;

	public TransPreviewDelegate transPreviewDelegate;

	/** A map that keeps track of which log line was written by which step */
	private Map<StepMeta, String> stepLogMap;

	private StepMeta startHopStep;
	private Point endHopLocation;
	private boolean startErrorHopStep;

	private StepMeta noInputStep;

	private StepMeta endHopStep;

	private StreamType candidateHopType;

	private StepMeta showTargetStreamsStep;

	Timer redrawTimer;

	public Trans trans;
	private String carteObjectId;

	public void setCurrentNote(NotePadMeta ni) {
		this.ni = ni;
	}

	public NotePadMeta getCurrentNote() {
		return ni;
	}

	public TransHopMeta getCurrentHop() {
		return currentHop;
	}

	public void setCurrentHop(TransHopMeta currentHop) {
		this.currentHop = currentHop;
	}

	public StepMeta getCurrentStep() {
		return currentStep;
	}

	public void setCurrentStep(StepMeta currentStep) {
		this.currentStep = currentStep;
	}

	public TransGraph(GraphEditor ge, final TransMeta transMeta) {
		this.ge = ge;
		this.transMeta = transMeta;
		init();
	}
	
	public TransGraph(GraphEditor ge, final Trans trans, String carteObjectId) {
		this.ge = ge;
		this.trans = trans;
		this.transMeta = trans.getTransMeta();
		this.carteObjectId = carteObjectId;
		init();
	}

	private void init() {
		this.areaOwners = new ArrayList<AreaOwner>();
		this.log = ge.getLog();

		transLogDelegate = new TransLogDelegate(ge, this);
		transGridDelegate = new TransGridDelegate(ge, this);
		transHistoryDelegate = new TransHistoryDelegate(ge, this);
		transPerfDelegate = new TransPerfDelegate(ge, this);
		transMetricsDelegate = new TransMetricsDelegate(ge, this);
		transPreviewDelegate = new TransPreviewDelegate(ge, this);
	}

	public synchronized void debug(
			TransExecutionConfiguration executionConfiguration,
			TransDebugMeta transDebugMeta) {
		if (!running) {
			try {
				this.lastTransDebugMeta = transDebugMeta;

				log.setLogLevel(executionConfiguration.getLogLevel());
				if (log.isDetailed()) {
					log.logDetailed(BaseMessages.getString(PKG,
							"TransLog.Log.DoPreview"));
				}
				String[] args = null;
				Map<String, String> arguments = executionConfiguration
						.getArguments();
				if (arguments != null) {
					args = convertArguments(arguments);
				}
				transMeta
						.injectVariables(executionConfiguration.getVariables());

				// Set the named parameters
				Map<String, String> paramMap = executionConfiguration
						.getParams();
				Set<String> keys = paramMap.keySet();
				for (String key : keys) {
					transMeta.setParameterValue(key,
							Const.NVL(paramMap.get(key), ""));
				}

				transMeta.activateParameters();

				// Do we need to clear the log before running?
				//
				if (executionConfiguration.isClearingLog()) {
					transLogDelegate.clearLog();
				}

				// Do we have a previous execution to clean up in the logging
				// registry?
				//
				if (trans != null) {
					KettleLogStore.discardLines(trans.getLogChannelId(), false);
					LoggingRegistry.getInstance().removeIncludingChildren(
							trans.getLogChannelId());
				}

				// Create a new transformation to execution
				//
				trans = new Trans(transMeta);
				trans.setSafeModeEnabled(executionConfiguration
						.isSafeModeEnabled());
				trans.setPreview(true);
				trans.setGatheringMetrics(executionConfiguration
						.isGatheringMetrics());
				trans.prepareExecution(args);
				trans.setRepository(ge.getRepository());

				// Add the row listeners to the allocated threads
				//
				transDebugMeta.addRowListenersToTransformation(trans);

				// What method should we call back when a break-point is hit?
				//
				transDebugMeta.addBreakPointListers(new BreakPointListener() {
					@Override
					public void breakPointHit(TransDebugMeta transDebugMeta,
							StepDebugMeta stepDebugMeta,
							RowMetaInterface rowBufferMeta,
							List<Object[]> rowBuffer) {
						// showPreview( transDebugMeta, stepDebugMeta,
						// rowBufferMeta, rowBuffer );
					}
				});

				// Do we capture data?
				//
				if (transPreviewDelegate.isActive()) {
					transPreviewDelegate.capturePreviewData(trans,
							transMeta.getSteps());
				}

				// Start the threads for the steps...
				//
				startThreads();

				debug = true;

				// Show the execution results view...
				//
				new Thread(new Runnable() {
					@Override
					public void run() {
						startAllDelegates();
					}
				}).start();
				;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
		}
	}

	public void start(TransExecutionConfiguration executionConfiguration)
			throws KettleException {
		log.logMinimal(BaseMessages.getString(PKG,
				"TransLog.Log.LaunchingTransformation")
				+ trans.getTransMeta().getName() + "]...");

		trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());
		trans.setGatheringMetrics(executionConfiguration.isGatheringMetrics());

		// Launch the step preparation in a different thread.
		// That way Spoon doesn't block anymore and that way we can follow the
		// progress of the initialization
		//
		final Thread parentThread = Thread.currentThread();
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				startAllDelegates();
				prepareTrans(parentThread, null);
			}
		});
		
		log.logMinimal(BaseMessages.getString(PKG,
				"TransLog.Log.StartedExecutionOfTransformation"));
	}

	public void startAllDelegates() {
		transHistoryDelegate.scheduleHistoryDataCollection();
		transLogDelegate.scheduleLogDataCollection();
		transGridDelegate.scheduleGridDataCollection();
		transPerfDelegate.schedulePerfDataCollection();
		transMetricsDelegate.scheduleMetricsDataCollection();
		transPreviewDelegate.schedulePreviewDataCollection();
	}

	private String[] convertArguments(Map<String, String> arguments) {
		String[] argumentNames = arguments.keySet().toArray(
				new String[arguments.size()]);
		Arrays.sort(argumentNames);

		String[] args = new String[argumentNames.length];
		for (int i = 0; i < args.length; i++) {
			String argumentName = argumentNames[i];
			args[i] = arguments.get(argumentName);
		}
		return args;
	}

	public void stop() {
		if (running && !halting) {
			halting = true;
			trans.stopAll();
			log.logMinimal(BaseMessages.getString(PKG,
					"TransLog.Log.ProcessingOfTransformationStopped"));

			running = false;
			initialized = false;
			halted = false;
			halting = false;

			transMeta.setInternalKettleVariables(); // set the original vars
													// back as they may be
													// changed by a mapping
		}
	}

	public synchronized void pauseResume() {
		if (running) {
			// Get the pause toolbar item
			//
			if (!pausing) {
				pausing = true;
				trans.pauseRunning();
			} else {
				pausing = false;
				trans.resumeRunning();
			}
		}
	}

	private synchronized void prepareTrans(final Thread parentThread,
			final String[] args) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					trans.prepareExecution(args);

					// Do we capture data?
					//
					if (transPreviewDelegate.isActive()) {
						transPreviewDelegate.capturePreviewData(trans,
								transMeta.getSteps());
					}

					initialized = true;
				} catch (KettleException e) {
					log.logError(trans.getName()
							+ ": preparing transformation execution failed", e);
				}
				halted = trans.hasHaltedSteps();
				if (trans.isReadyToStart()) {
					checkStartThreads(); // After init, launch the threads.
				} else {
					initialized = false;
					running = false;
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	private void checkStartThreads() {
		if (initialized && !running && trans != null) {
			startThreads();
		}
	}

	private synchronized void startThreads() {
		running = true;
		try {
			// Add a listener to the transformation.
			// If the transformation is done, we want to do the end processing,
			// etc.
			//
			trans.addTransListener(new TransAdapter() {

				@Override
				public void transFinished(Trans trans) {
					checkTransEnded();
					stopRedrawTimer();

					transMetricsDelegate.resetLastRefreshTime();
					transMetricsDelegate.updateGraph();
					
					transGridDelegate.stopGridDataCollection();
					transLogDelegate.stopLogDataCollection();
					
					String json = GEFinishedResponseEncoderDecoder.INSTANCE.encode((new GEFinishedResponse(trans.getErrors(),carteObjectId)));
					ge.broadcast(null, json);
				}
			});

			trans.startThreads();

		} catch (KettleException e) {
			log.logError("Error starting step threads", e);
		}
	}

	protected void stopRedrawTimer() {
		if (redrawTimer != null) {
			redrawTimer.cancel();
			redrawTimer.purge();
			redrawTimer = null;
		}

	}

	private void checkTransEnded() {
		if (trans != null) {
			if (trans.isFinished() && (running || halted)) {
				log.logMinimal(BaseMessages.getString(PKG,
						"TransLog.Log.TransformationHasFinished"));

				running = false;
				initialized = false;
				halted = false;
				halting = false;

				// setControlStates();

				// OK, also see if we had a debugging session going on.
				// If so and we didn't hit a breakpoint yet, display the show
				// preview dialog...
				//
				if (debug && lastTransDebugMeta != null
						&& lastTransDebugMeta.getTotalNumberOfHits() == 0) {
					debug = false;
					showLastPreviewResults();
				}
				debug = false;
			}
		}
	}

	public synchronized void showLastPreviewResults() {
		if (lastTransDebugMeta == null
				|| lastTransDebugMeta.getStepDebugMetaMap().isEmpty()) {
			return;
		}

		final List<String> stepnames = new ArrayList<String>();
		final List<RowMetaInterface> rowMetas = new ArrayList<RowMetaInterface>();
		final List<List<Object[]>> rowBuffers = new ArrayList<List<Object[]>>();

		// Assemble the buffers etc in the old style...
		//
		for (StepMeta stepMeta : lastTransDebugMeta.getStepDebugMetaMap()
				.keySet()) {
			StepDebugMeta stepDebugMeta = lastTransDebugMeta
					.getStepDebugMetaMap().get(stepMeta);

			stepnames.add(stepMeta.getName());
			rowMetas.add(stepDebugMeta.getRowBufferMeta());
			rowBuffers.add(stepDebugMeta.getRowBuffer());
		}
	}

	/**
	 * XUL dummy menu entry
	 */
	public void openMapping() {
	}

	/**
	 * Finds the last active transformation in the running job to the opened
	 * transMeta
	 *
	 * @param transGraph
	 * @param jobEntryCopy
	 */
	private Trans getActiveSubtransformation(TransGraph transGraph,
			StepMeta stepMeta) {
		if (trans != null && transGraph != null) {
			return trans.getActiveSubtransformations().get(stepMeta.getName());
		}
		return null;
	}

	/**
	 * Finds the last active job in the running transformation to the opened
	 * jobMeta
	 *
	 * @param jobGraph
	 * @param stepMeta
	 */
	private void attachActiveJob(JobGraph jobGraph, StepMeta stepMeta) {
		if (trans != null && jobGraph != null) {
			Job subJob = trans.getActiveSubjobs().get(stepMeta.getName());
			jobGraph.setJob(subJob);
			jobGraph.showExecutionResults();
		}
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running
	 *            the running to set
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * @return the lastTransDebugMeta
	 */
	public TransDebugMeta getLastTransDebugMeta() {
		return lastTransDebugMeta;
	}

	/**
	 * @return the halting
	 */
	public boolean isHalting() {
		return halting;
	}

	/**
	 * @param halting
	 *            the halting to set
	 */
	public void setHalting(boolean halting) {
		this.halting = halting;
	}

	/**
	 * @return the stepLogMap
	 */
	public Map<StepMeta, String> getStepLogMap() {
		return stepLogMap;
	}

	/**
	 * @param stepLogMap
	 *            the stepLogMap to set
	 */
	public void setStepLogMap(Map<StepMeta, String> stepLogMap) {
		this.stepLogMap = stepLogMap;
	}

	public void dumpLoggingRegistry() {
		LoggingRegistry registry = LoggingRegistry.getInstance();
		Map<String, LoggingObjectInterface> loggingMap = registry.getMap();

		for (LoggingObjectInterface loggingObject : loggingMap.values()) {
			System.out.println(loggingObject.getLogChannelId() + " - "
					+ loggingObject.getObjectName() + " - "
					+ loggingObject.getObjectType());
		}

	}

	public HasLogChannelInterface getLogChannelProvider() {
		return trans;
	}

	public void setTrans(Trans trans) {
		this.trans = trans;
		if (trans != null) {
			pausing = trans.isPaused();
			initialized = trans.isInitializing();
			running = trans.isRunning();
			halted = trans.isStopped();

			if (running) {
				trans.addTransListener(new TransAdapter() {
					@Override
					public void transFinished(Trans trans) {
						checkTransEnded();
					}
				});
			}
		}
	}

	public String getName() {
		return "transgraph";
	}

	public boolean canHandleSave() {
		return true;
	}

	private static double dampningConstant = 0.5;
	// private static double springConstant = 1.0;
	private static double timeStep = 1.0;
	private static double nodeMass = 1.0;

	public TransMeta getTransMeta() {
		return transMeta;
	}

	public Trans getTrans() {
		return trans;
	}
}
