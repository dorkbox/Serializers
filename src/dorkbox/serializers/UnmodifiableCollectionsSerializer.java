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

import java.util.ArrayList;
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
 * A kryo {@link Serializer} for unmodifiable {@link Collection}s and {@link Map}s
 * created via {@link Collections}.
 * 
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 * @author <a href="mailto:email@dorkbox.com">Dorkbox llc</a>
 */
public class UnmodifiableCollectionsSerializer extends Serializer<Object> {

    /**
     * Gets the version number.
     */
    public static
    String getVersion() {
        return "3.0";
    }


    static {
        // Add this project to the updates system, which verifies this class + UUID + version information
        dorkbox.updates.Updates.INSTANCE.add(UnmodifiableCollectionsSerializer.class, "316353f5338341a8a3edc01d702703f8", getVersion());

        try {
            ClassPool pool = ClassPool.getDefault();

            // allow non-reflection access to java.util.Collections...()
            {
                CtClass dynamicClass = pool.makeClass("java.util.UnmodifiableCollectionsAccessory");
                CtMethod method = CtNewMethod.make(
                        "public static Object getUnmodifiableCollectionField(Object nativeComp) { " +
                        // "java.lang.System.err.println(\"Getting collection field!\" + ((java.util.Collections$UnmodifiableCollection)nativeComp).c);" +
                        "return ((java.util.Collections$UnmodifiableCollection)nativeComp).c;" +
                        "}", dynamicClass);
                dynamicClass.addMethod(method);

                method = CtNewMethod.make(
                        "public static java.lang.reflect.Field initUnmodifiableMapField() { " +
                        "java.lang.reflect.Field field = Class.forName(\"java.util.Collections$UnmodifiableMap\").getDeclaredField( \"m\" );" +
                        "field.setAccessible( true );" +
                        // "java.lang.System.err.println(\"updating map field!\");" +
                        "return field;" +
                        "}", dynamicClass);
                dynamicClass.addMethod(method);

                method = CtNewMethod.make(
                        "public static Object getUnmodifiableMapField(java.lang.reflect.Field field, Object nativeComp) { " +
                        // "java.lang.System.err.println(\"Getting map field!\" + field.get(nativeComp));" +
                        "return field.get(nativeComp);" +
                        "}", dynamicClass);
                dynamicClass.addMethod(method);

                final byte[] dynamicClassBytes = dynamicClass.toBytecode();
                ClassUtils.defineClass(null, dynamicClassBytes);
            }

            // fix the accessor class to point to the generated proxy/accessory class
            {
                CtClass classFixer = pool.get("dorkbox.serializers.UnmodifiableCollectionJavaAccessor");

                CtMethod ctMethod = classFixer.getDeclaredMethod("UnmodifiableCollection_Field");
                ctMethod.setBody("{" +
                                 "return java.util.UnmodifiableCollectionsAccessory.getUnmodifiableCollectionField($1);" +
                                 "}");
                // perform pre-verification for the modified method
                ctMethod.getMethodInfo().rebuildStackMapForME(pool);


                ctMethod = classFixer.getDeclaredMethod("initUnmodifiableMap_Field");
                ctMethod.setBody("{" +
                                 "dorkbox.serializers.UnmodifiableCollectionJavaAccessor.SOURCE_MAP_FIELD = java.util.UnmodifiableCollectionsAccessory.initUnmodifiableMapField();" +
                                 "}");
                // perform pre-verification for the modified method
                ctMethod.getMethodInfo().rebuildStackMapForME(pool);


                ctMethod = classFixer.getDeclaredMethod("UnmodifiableMap_Field");
                ctMethod.setBody("{" +
                                 "return java.util.UnmodifiableCollectionsAccessory.getUnmodifiableMapField(dorkbox.serializers.UnmodifiableCollectionJavaAccessor.SOURCE_MAP_FIELD, $1);" +
                                 "}");
                // perform pre-verification for the modified method
                ctMethod.getMethodInfo().rebuildStackMapForME(pool);


                // perform pre-verification for the modified method
                ctMethod.getMethodInfo().rebuildStackMapForME(pool);


                final byte[] classFixerBytes = classFixer.toBytecode();
                ClassUtils.defineClass(ClassLoader.getSystemClassLoader(), classFixerBytes);
            }


            // setup reflection, but it's done from INSIDE THE SAME PACKAGE (so no warnings/etc)
            UnmodifiableCollectionJavaAccessor.initUnmodifiableMap_Field();

        } catch ( final Exception e ) {
            throw new RuntimeException( "Could not modify UnmodifiableCollection", e );
        }
    }

    @Override
    public Object read(final Kryo kryo, final Input input, final Class<? extends Object> clazz) {
        final int ordinal = input.readInt( true );
        final UnmodifiableCollection unmodifiableCollection = UnmodifiableCollection.values()[ordinal];
        final Object sourceCollection = kryo.readClassAndObject( input );
        return unmodifiableCollection.create( sourceCollection );
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Object object) {
        try {
            final UnmodifiableCollection unmodifiableCollection = UnmodifiableCollection.valueOfType( object.getClass() );

            // the ordinal could be replaced by something else (e.g. an explicitly managed "id")
            output.writeInt( unmodifiableCollection.ordinal(), true );
            kryo.writeClassAndObject( output, unmodifiableCollection.getValue(object) );
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
            final UnmodifiableCollection unmodifiableCollection = UnmodifiableCollection.valueOfType( original.getClass() );
            Object sourceCollectionCopy = kryo.copy(unmodifiableCollection.getValue(original));
            return unmodifiableCollection.create( sourceCollectionCopy );
        } catch ( final RuntimeException e ) {
            // Don't eat and wrap RuntimeExceptions
            throw e;
        } catch ( final Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private enum UnmodifiableCollection {
        COLLECTION( Collections.unmodifiableCollection(Collections.singletonList("")).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.unmodifiableCollection( (Collection<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return UnmodifiableCollectionJavaAccessor.UnmodifiableCollection_Field(sourceCollection);
            }
        },
        RANDOM_ACCESS_LIST( Collections.unmodifiableList( new ArrayList<Void>() ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.unmodifiableList( (List<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return UnmodifiableCollectionJavaAccessor.UnmodifiableCollection_Field(sourceCollection);
            }
        },
        LIST( Collections.unmodifiableList( new LinkedList<Void>() ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.unmodifiableList( (List<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return UnmodifiableCollectionJavaAccessor.UnmodifiableCollection_Field(sourceCollection);
            }
        },
        SET( Collections.unmodifiableSet( new HashSet<Void>() ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.unmodifiableSet( (Set<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return UnmodifiableCollectionJavaAccessor.UnmodifiableCollection_Field(sourceCollection);
            }
        },
        SORTED_SET( Collections.unmodifiableSortedSet( new TreeSet<Void>() ).getClass() ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.unmodifiableSortedSet( (SortedSet<?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return UnmodifiableCollectionJavaAccessor.UnmodifiableCollection_Field(sourceCollection);
            }
        },
        MAP( Collections.unmodifiableMap( new HashMap<Void, Void>() ).getClass() ) {

            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.unmodifiableMap( (Map<?, ?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return UnmodifiableCollectionJavaAccessor.UnmodifiableMap_Field(sourceCollection);
            }
        },
        SORTED_MAP( Collections.unmodifiableSortedMap( new TreeMap<Void, Void>() ).getClass() ) {
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.unmodifiableSortedMap( (SortedMap<?, ?>) sourceCollection );
            }
            @Override
            public Object getValue( final Object sourceCollection ) {
                return UnmodifiableCollectionJavaAccessor.UnmodifiableMap_Field(sourceCollection);
            }
        };
        
        private final Class<?> type;

        UnmodifiableCollection( final Class<?> type) {
            this.type = type;
        }
        
        /**
         * @param sourceCollection
         */
        public abstract Object create( Object sourceCollection );
        public abstract Object getValue( Object sourceCollection );


        static UnmodifiableCollection valueOfType( final Class<?> type ) {
            for( final UnmodifiableCollection item : values() ) {
                if ( item.type.equals( type ) ) {
                    return item;
                }
            }
            throw new IllegalArgumentException( "The type " + type + " is not supported." );
        }
    }

    /**
     * Creates a new {@link UnmodifiableCollectionsSerializer} and registers its serializer
     * for the several unmodifiable Collections that can be created via {@link Collections},
     * including {@link Map}s.
     * 
     * @param kryo the {@link Kryo} instance to set the serializer on.
     * 
     * @see Collections#unmodifiableCollection(Collection)
     * @see Collections#unmodifiableList(List)
     * @see Collections#unmodifiableSet(Set)
     * @see Collections#unmodifiableSortedSet(SortedSet)
     * @see Collections#unmodifiableMap(Map)
     * @see Collections#unmodifiableSortedMap(SortedMap)
     */
    public static void registerSerializers( final Kryo kryo ) {
        final UnmodifiableCollectionsSerializer serializer = new UnmodifiableCollectionsSerializer();
        UnmodifiableCollection[] values = UnmodifiableCollection.values();
        for ( final UnmodifiableCollection item : values) {
            kryo.register( item.type, serializer );
        }
    }
}
