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

package org.pentaho.di.www;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

public class BaseWebSocket {

  protected LogChannelInterface log = new LogChannel( "WebSocket" );

  public void logMinimal( String s ) {
    log.logMinimal( s );
  }

  public void logBasic( String s ) {
    log.logBasic( s );
  }

  public void logError( String s ) {
    log.logError( s );
  }

  public void logError( String s, Throwable e ) {
    log.logError( s, e );
  }

  public void logBasic( String s, Object... arguments ) {
    log.logBasic( s, arguments );
  }

  public void logDetailed( String s, Object... arguments ) {
    log.logDetailed( s, arguments );
  }

  public void logError( String s, Object... arguments ) {
    log.logError( s, arguments );
  }

  public void logDetailed( String s ) {
    log.logDetailed( s );
  }

  public void logDebug( String s ) {
    log.logDebug( s );
  }

  public void logRowlevel( String s ) {
    log.logRowlevel( s );
  }
}
