package org.pentaho.di.www.ge.serialization;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"rowMeta"})
public abstract class RowMetaAndDataMixIn {
}