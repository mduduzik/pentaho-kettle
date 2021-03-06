package org.pentaho.di.www.ge.json;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.deser.BeanDeserializer;
import org.codehaus.jackson.map.deser.BeanDeserializerBuilder;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.www.ge.serialization.RowMetaAndDataMixIn;
import org.pentaho.di.www.ge.serialization.RowMetaAndDataSerializer;
import org.pentaho.di.www.ge.serialization.ValueMetaInterfaceSerializer;

import java.util.ArrayList;
import java.util.List;

public class CustomObjectMapper extends ObjectMapper {
    private final SimpleModule module;
    //private final SimpleModule module;
    private SimpleFilterProvider filters;

    public CustomObjectMapper() {
        super(null,null, new StdDeserializerProvider() {
            @Override
			protected KeyDeserializer _handleUnknownKeyDeserializer(JavaType type)
                    throws JsonMappingException
            {
                return null;
            }

            @Override
			public StdDeserializerProvider withFactory(DeserializerFactory factory) {
                // sanity-check to try to prevent hard-to-debug problems; sub-classes MUST override this method
                return null;
            }

            @Override
			protected JsonDeserializer<Object> _handleUnknownValueDeserializer(JavaType type)
                    throws JsonMappingException
            {
                // Let's try to figure out the reason, to give better error
                Class<?> rawClass = type.getRawClass();
                if (!ClassUtil.isConcrete(rawClass)) {
                    return null;//throw new JsonMappingException("Can not find a Value deserializer for abstract type "+type);
                }
                throw new JsonMappingException("Can not find a Value deserializer for type "+type);
            }

            /**
             * Method that handles actual construction (via factory) and caching (both
             * intermediate and eventual)
             */
            @Override
			protected JsonDeserializer<Object> _createAndCache2(DeserializationConfig config, JavaType type,
                                                                BeanProperty property)
                    throws JsonMappingException
            {
                JsonDeserializer<Object> deser = null;
                try {
                    deser = _createDeserializer(config, type, property);
                } catch (IllegalArgumentException iae) {
            /* We better only expose checked exceptions, since those
             * are what caller is expected to handle
             */
                    //throw new JsonMappingException(iae.getMessage(), null, iae);
                }
                if (deser == null) {
                    return null;
                }
        /* cache resulting deserializer? always true for "plain" BeanDeserializer
         * (but can be re-defined for sub-classes by using @JsonCachable!)
         */
                // 08-Jun-2010, tatu: Related to [JACKSON-296], need to avoid caching MapSerializers... so:
                boolean isResolvable = (deser instanceof ResolvableDeserializer);
                boolean addToCache = (deser.getClass() == BeanDeserializer.class);
                if (!addToCache) {
                    // 14-Feb-2011, tatu: As per [JACKSON-487], try fully blocking annotation access:
                    if (config.isEnabled(DeserializationConfig.Feature.USE_ANNOTATIONS)) {
                        AnnotationIntrospector aintr = config.getAnnotationIntrospector();
                        // note: pass 'null' to prevent mix-ins from being used
                        AnnotatedClass ac = AnnotatedClass.construct(deser.getClass(), aintr, null);
                        Boolean cacheAnn = aintr.findCachability(ac);
                        if (cacheAnn != null) {
                            addToCache = cacheAnn.booleanValue();
                        }
                    }
                }
        /* we will temporarily hold on to all created deserializers (to
         * handle cyclic references, and possibly reuse non-cached
         * deserializers (list, map))
         */
        /* 07-Jun-2010, tatu: Danger: [JACKSON-296] was caused by accidental
         *   resolution of a reference -- couple of ways to prevent this;
         *   either not add Lists or Maps, or clear references eagerly.
         *   Let's actually do both; since both seem reasonable.
         */
        /* Need to resolve? Mostly done for bean deserializers; required for
         * resolving cyclic references.
         */
                if (isResolvable) {
                    _incompleteDeserializers.put(type, deser);
                    _resolveDeserializer(config, (ResolvableDeserializer)deser);
                    _incompleteDeserializers.remove(type);
                }
                if (addToCache) {
                    _cachedDeserializers.put(type, deser);
                }
                return deser;
            }
        },null,new DeserializationConfig(new CustomBasicClassIntrospector(), DEFAULT_ANNOTATION_INTROSPECTOR, STD_VISIBILITY_CHECKER,
                null, null, TypeFactory.defaultInstance(), null));

/*        setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        setVisibility(JsonMethod.GETTER, JsonAutoDetect.Visibility.NONE);
        setVisibility(JsonMethod.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        setVisibility(JsonMethod.SETTER, JsonAutoDetect.Visibility.NONE);
        this.module = new SimpleModule(getClass().getName(),new Version(1, 0, 0, null));
        this.module.addDeserializer(TextFileInputMeta.class,new GenericBeanDeserializer());
        this.module.addKeyDeserializer(JobEntryCopy.class,new JobEntryCopyKeyDeserializer());
        registerModule(module);*/
        this.module = new SimpleModule(getClass().getName(),new Version(1, 0, 0, null));

        //Visibility
        setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        setVisibility(JsonMethod.GETTER, JsonAutoDetect.Visibility.NONE);
        setVisibility(JsonMethod.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        setVisibility(JsonMethod.SETTER, JsonAutoDetect.Visibility.NONE);


        //Deserialization features
        getDeserializationConfig().without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        getDeserializationConfig().without(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS);


        //Serialization features
        getSerializationConfig().with(SerializationConfig.Feature.INDENT_OUTPUT);
        getSerializationConfig().with(SerializationConfig.Feature.REQUIRE_SETTERS_FOR_GETTERS);

        //RowMetaAndData
        List<RowMetaAndData> type = new ArrayList<RowMetaAndData>(); 
        this.module.addSerializer(RowMetaAndData.class,new RowMetaAndDataSerializer());
        getDeserializationConfig().addMixInAnnotations(RowMetaAndData.class, RowMetaAndDataMixIn.class);
        getSerializationConfig().addMixInAnnotations(RowMetaAndData.class, RowMetaAndDataMixIn.class);

        //ValueMetaInterface
        this.module.addSerializer(ValueMetaInterface.class,new ValueMetaInterfaceSerializer());        
        registerModule(module);
    }


    public ObjectWriter getFilteredWriter() {
       return this.writer(this.filters);
    }

    private static class BeanDeserializerModifierForIgnorables extends BeanDeserializerModifier {

        private Class<?> type;
        private List<String> ignorables;

        public BeanDeserializerModifierForIgnorables(Class clazz, String... properties) {
            ignorables = new ArrayList<String>();
            for(String property : properties) {
                ignorables.add(property);
            }
            this.type = clazz;
        }


        @Override
        public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BasicBeanDescription beanDesc, BeanDeserializerBuilder builder) {
            if(!type.equals(beanDesc.getBeanClass())) {
                return builder;
            }

            for(String ignorable : ignorables) {
                builder.addIgnorable(ignorable);
            }

            return builder;
        }

        @Override
        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BasicBeanDescription beanDesc, JsonDeserializer<?> deserializer) {
            return deserializer;
        }
    }
}
