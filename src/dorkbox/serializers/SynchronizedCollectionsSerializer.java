/*
 * Copyright 2023 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2010 Martin Grotzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package dorkbox.serializers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import dorkbox.jna.ClassUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;

/**
 * A kryo {@link Serializer} for synchronized {@link Collection}s and {@link Map}s
 * created via {@link Collections}.
 * 
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 * @author <a href="mailto:email@dorkbox.com">Dorkbox llc</a>
 */
public class SynchronizedCollectionsSerializer extends Serializer<Object> {

    /**
     * Gets the version number.
     */
    public static
    String getVersion() {
        return "3.0";
    }

    static {
        dorkbox.updates.Updates.INSTANCE.add(SynchronizedCollectionsSerializer.class, "9e3f0fdbd0ac4e4ba887964733fe110a", getVersion());

        try {
            ClassPool pool = ClassPool.getDefault();

            // allow non-reflection access to java.util.Collections...()
            {
                CtClass dynamicClass = pool.makeClass("java.util.SynchronizedCollectionsAccessory");
                CtMethod method = CtNewMethod.make(
                        "public static Object getSynchronizedCollectionField(Object nativeComp) { " +
                        // "java.lang.System.err.println(\"Getting sync collection field!\" + ((java.util.Collections$SynchronizedCollection)nativeComp).c);" +
                        "return ((java.util.Collections$SynchronizedCollection)nativeComp).c;" +
                        "}", dynamicClass);
                dynamicClass.addMethod(method);

                method = CtNewMethod.make(
                        "public static java.lang.reflect.Field initSynchronizedMapField() { " +
                        "java.lang.reflect.Field field = Class.forName(\"java.util.Collections$SynchronizedMap\").getDeclaredField( \"m\" );" +
                        "field.setAccessible( true );" +
                        // "java.lang.System.err.println(\"updating sync map field!\");" +
                        "return field;" +
                        "}", dynamicClass);
                dynamicClass.addMethod(method);

                method = CtNewMethod.make(
                        "public static Object getSynchronizedMapField(java.lang.reflect.Field field, Object nativeComp) { " +
                        // "java.lang.System.err.println(\"Getting sync map field!\" + field.get(nativeComp));" +
                        "return field.get(nativeComp);" +
                        "}", dynamicClass);
                dynamicClass.addMethod(method);

                final byte[] dynamicClassBytes = dynamicClass.toBytecode();
                ClassUtils.defineClass(null, dynamicClassBytes);
            }

            // fix the accessor class to point to the generated proxy/accessory class
            {
                CtClass classFixer = pool.get("dorkbox.serializers.SynchronizedCollectionJavaAccessor");

                CtMethod ctMethod = classFixer.getDeclaredMethod("SynchronizedCollection_Field");
                ctMethod.setBody("{" +
                                 "return java.util.SynchronizedCollectionsAccessory.getSynchronizedCollectionField($1);" +
                                 "}");
                // perform pre-verification for the modified method
                ctMethod.getMethodInfo().rebuildStackMapForME(pool);


                ctMethod = classFixer.getDeclaredMethod("initSynchronizedMap_Field");
                ctMethod.setBody("{" +
                                 "dorkbox.serializers.SynchronizedCollectionJavaAccessor.SOURCE_MAP_FIELD = java.util.SynchronizedCollectionsAccessory.initSynchronizedMapField();" +
                                 "}");
                // perform pre-verification for the modified method
                ctMethod.getMethodInfo().rebuildStackMapForME(pool);


                ctMethod = classFixer.getDeclaredMethod("SynchronizedMap_Field");
                ctMethod.setBody("{" +
                                 "return java.util.SynchronizedCollectionsAccessory.getSynchronizedMapField(dorkbox.serializers.SynchronizedCollectionJavaAccessor.SOURCE_MAP_FIELD, $1);" +
                                 "}");
                // perform pre-verification for the modified method
                ctMethod.getMethodInfo().rebuildStackMapForME(pool);


                // perform pre-verification for the modified method
                ctMethod.getMethodInfo().rebuildStackMapForME(pool);


                final byte[] classFixerBytes = classFixer.toBytecode();
                ClassUtils.defineClass(ClassLoader.getSystemClassLoader(), classFixerBytes);
            }


            // setup reflection, but it's done from INSIDE THE SAME PACKAGE (so no warnings/etc)
            SynchronizedCollectionJavaAccessor.initSynchronizedMap_Field();

        } catch ( final Exception e ) {
            throw new RuntimeException( "Could not modify SynchronizedCollection", e );
        }
    }

    @Override
    public Object read(final Kryo kryo, final Input input, final Class<? extends Object> clazz) {
        final int ordinal = input.readInt( true );
        final SynchronizedCollection collection = SynchronizedCollection.values()[ordinal];
        final Object sourceCollection = kryo.readClassAndObject( input );
        return collection.create( sourceCollection );
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Object object) {
        try {
            final SynchronizedCollection collection = SynchronizedCollection.valueOfType( object.getClass() );
            // the ordinal could be replaced by something else (e.g. a explicitly managed "id")
            output.writeInt( collection.ordinal(), true );
            synchronized (object) {
                kryo.writeClassAndObject( output, collection.getValue( object ) );
            }
        } catch ( final RuntimeException e ) {
            // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
            // handles SerializationException specifically (resizing the buffer)...
            throw e;
        } catch ( final Exception e ) {
            throw new RuntimeException( e );
        }
    }
    
    @Override
    public Object copy(Kryo kryo, Object original) {
      try {
          final SynchronizedCollection collection = SynchronizedCollection.valueOfType( original.getClass() );
          Object sourceCollectionCopy = kryo.copy(collection.getValue(original));
          return collection.create( sourceCollectionCopy );
      } catch ( final RuntimeException e ) {
          // Don't eat and wrap RuntimeExceptions
          throw e;
      } catch ( final Exception e ) {
          throw new RuntimeException( e );
      }
    }

    private static enum SynchronizedCollection {
        COLLECTION( Collections.synchronizedCollection( Arrays.asList( "" ) ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedCollection( (Collection<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return SynchronizedCollectionJavaAccessor.SynchronizedCollection_Field(sourceCollection );
            }
        },
        RANDOM_ACCESS_LIST( Collections.synchronizedList( new ArrayList<Void>() ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedList( (List<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return SynchronizedCollectionJavaAccessor.SynchronizedCollection_Field(sourceCollection );
            }
        },
        LIST( Collections.synchronizedList( new LinkedList<Void>() ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedList( (List<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return SynchronizedCollectionJavaAccessor.SynchronizedCollection_Field(sourceCollection );
            }
        },
        SET( Collections.synchronizedSet( new HashSet<Void>() ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedSet( (Set<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return SynchronizedCollectionJavaAccessor.SynchronizedCollection_Field(sourceCollection );
            }
        },
        SORTED_SET( Collections.synchronizedSortedSet( new TreeSet<Void>() ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedSortedSet( (SortedSet<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return SynchronizedCollectionJavaAccessor.SynchronizedCollection_Field(sourceCollection );
            }
        },
        MAP( Collections.synchronizedMap( new HashMap<Void, Void>() ).getClass() ) {

            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedMap( (Map<?, ?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return SynchronizedCollectionJavaAccessor.SynchronizedMap_Field(sourceCollection );
            }
        },
        SORTED_MAP( Collections.synchronizedSortedMap( new TreeMap<Void, Void>() ).getClass() ) {
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedSortedMap( (SortedMap<?, ?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return SynchronizedCollectionJavaAccessor.SynchronizedMap_Field(sourceCollection );
            }
        };
        
        private final Class<?> type;

        private SynchronizedCollection( final Class<?> type ) {
            this.type = type;
        }
        
        /**
         * @param sourceCollection
         */
        public abstract Object create( Object sourceCollection );
        public abstract Object getValue( Object sourceCollection );

        static SynchronizedCollection valueOfType( final Class<?> type ) {
            for( final SynchronizedCollection item : values() ) {
                if ( item.type.equals( type ) ) {
                    return item;
                }
            }
            throw new IllegalArgumentException( "The type " + type + " is not supported." );
        }
        
    }

    /**
     * Creates a new {@link SynchronizedCollectionsSerializer} and registers its serializer
     * for the several synchronized Collections that can be created via {@link Collections},
     * including {@link Map}s.
     * 
     * @param kryo the {@link Kryo} instance to set the serializer on.
     * 
     * @see Collections#synchronizedCollection(Collection)
     * @see Collections#synchronizedList(List)
     * @see Collections#synchronizedSet(Set)
     * @see Collections#synchronizedSortedSet(SortedSet)
     * @see Collections#synchronizedMap(Map)
     * @see Collections#synchronizedSortedMap(SortedMap)
     */
    public static void registerSerializers( final Kryo kryo ) {
        final SynchronizedCollectionsSerializer serializer = new SynchronizedCollectionsSerializer();
        SynchronizedCollection[] values = SynchronizedCollection.values();
        for ( final SynchronizedCollection item : values) {
            kryo.register( item.type, serializer );
        }
    }
}
