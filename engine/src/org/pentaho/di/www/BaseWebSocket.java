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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseWebSocket {

  protected Logger log = LoggerFactory.getLogger(this.getClass());

  public void logMinimal( String s ) {
    log.info( s );
  }

  public void logBasic( String s ) {
    log.info( s );
  }

  public void logError( String s ) {
    log.error( s );
  }

  public void logError( String s, Throwable e ) {
    log.error( s, e );
  }

  public void logBasic( String s, Object... arguments ) {
    log.info( s, arguments );
  }

  public void logDetailed( String s, Object... arguments ) {
    log.trace( s, arguments );
  }

  public void logError( String s, Object... arguments ) {
    log.error( s, arguments );
  }

  public void logDetailed( String s ) {
    log.trace( s );
  }

  public void logDebug( String s ) {
    log.debug( s );
  }

  public void logRowlevel( String s ) {
    log.trace( s );
  }
}
