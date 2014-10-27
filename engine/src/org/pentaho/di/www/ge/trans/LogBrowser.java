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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringEscapeUtils;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.KettleLogLayout;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogParentProvidedInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.www.ge.GraphEditor;
import org.pentaho.di.www.ge.websocket.message.trans.GETransLogUpdate;
import org.pentaho.di.www.ge.websocket.message.trans.GETransLogUpdateEncoderDecoder;

public class LogBrowser {
	private static Class<?> PKG = GraphEditor.class; // for i18n purposes,
														// needed by
														// Translator2!!

	private LogParentProvidedInterface logProvider;
	private List<String> childIds = new ArrayList<String>();
	private Date lastLogRegistryChange;
	private AtomicBoolean paused;

	private TimerTask timerTask;

	private GraphEditor ge;

	public LogBrowser(final LogParentProvidedInterface logProvider,
			GraphEditor ge) {
		this.logProvider = logProvider;
		this.ge = ge;
		this.paused = new AtomicBoolean(false);
	}

	public void installLogSniffer() {
		// Create a new buffer appender to the log and capture that directly...
		//
		final AtomicInteger lastLogId = new AtomicInteger(-1);
		final AtomicBoolean busy = new AtomicBoolean(false);
		final KettleLogLayout logLayout = new KettleLogLayout(true);

		/*
		 * final StyleRange normalLogLineStyle = new StyleRange();
		 * normalLogLineStyle.foreground =
		 * GUIResource.getInstance().getColorBlue(); final StyleRange
		 * errorLogLineStyle = new StyleRange(); errorLogLineStyle.foreground =
		 * GUIResource.getInstance().getColorRed();
		 */

		// Refresh the log every second or so
		//
		final Timer logRefreshTimer = new Timer("log sniffer Timer");
		timerTask = new TimerTask() {
			public void run() {
				new Thread(new Runnable() {
					public void run() {
						HasLogChannelInterface provider = logProvider.getLogChannelProvider();

						if (provider != null && !busy.get() && !paused.get()) {
							busy.set(true);

							LogChannelInterface logChannel = provider
									.getLogChannel();
							String parentLogChannelId = logChannel
									.getLogChannelId();
							LoggingRegistry registry = LoggingRegistry
									.getInstance();
							Date registryModDate = registry
									.getLastModificationTime();

							if (childIds == null
									|| lastLogRegistryChange == null
									|| registryModDate
											.compareTo(lastLogRegistryChange) > 0) {
								lastLogRegistryChange = registry
										.getLastModificationTime();
								childIds = LoggingRegistry.getInstance()
										.getLogChannelChildren(
												parentLogChannelId);
							}

							// See if we need to log any lines...
							//
							int lastNr = KettleLogStore.getLastBufferLineNr();
							if (lastNr > lastLogId.get()) {
								List<KettleLoggingEvent> logLines = KettleLogStore
										.getLogBufferFromTo(childIds, true,
												lastLogId.get(), lastNr);

								// int position = text.getSelection().x;
								// StringBuffer buffer = new
								// StringBuffer(text.getText());
								StringBuffer stringBuffer = new StringBuffer(
										10000);
								int index = lastLogId.get();
								String[] logLineArray;
								List<String[]> logLinesList = new ArrayList<String[]>();
								for (int i = 0; i < logLines.size(); i++) {
									logLineArray = new String[4];
									KettleLoggingEvent event = logLines.get(i);
									String line = logLayout.format(event)
											.trim();

									boolean hasError = (event.getLevel() == LogLevel.ERROR);

									logLineArray[0] = "" + (index++);
									logLineArray[1] = hasError ? "true"
											: "false";
									logLineArray[2] = StringEscapeUtils.escapeHtml(new Date(
											event.getTimeStamp()).toString());
									logLineArray[3] = StringEscapeUtils
											.escapeHtml(event.getMessage()
													.toString());

									logLinesList.add(logLineArray);

									lastLogId.set(lastNr);
								}
								ge.broadcast(null,
										GETransLogUpdateEncoderDecoder.INSTANCE
												.encode(new GETransLogUpdate(
														logLinesList)));
							}
							busy.set(false);
						}
					}
				}).start();
			}
		};

		// Refresh every often enough
		//
		logRefreshTimer.schedule(timerTask, 1000, 1000);
	}

	public void cancelLogSniffer() {
		timerTask.cancel();
	}

	public LogParentProvidedInterface getLogProvider() {
		return logProvider;
	}

	public boolean isPaused() {
		return paused.get();
	}

	public void setPaused(boolean paused) {
		this.paused.set(paused);
	}
}