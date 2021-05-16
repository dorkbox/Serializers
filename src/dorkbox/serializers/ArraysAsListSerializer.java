/*
 * Copyright 2021 dorkbox, llc
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
package dorkbox.serializers;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A kryo {@link Serializer} for lists created via {@link Arrays#asList(Object...)}.
 * <p>
 * Note: This serializer does not support cyclic references, so if one of the objects
 * gets set the list as attribute this might cause an error during deserialization.
 * </p>
 *
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public
class ArraysAsListSerializer extends Serializer<List<?>> {

    public
    ArraysAsListSerializer() {
    }

    @Override
    public
    List<?> read(final Kryo kryo, final Input input, final Class<? extends List<?>> type) {
        final int length = input.readInt(true);
        Class<?> componentType = kryo.readClass(input)
                                     .getType();
        if (componentType.isPrimitive()) {
            componentType = getPrimitiveWrapperClass(componentType);
        }
        try {
            final Object items = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(items, i, kryo.readClassAndObject(input));
            }
            return Arrays.asList((Object[]) items);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public
    void write(final Kryo kryo, final Output output, final List<?> obj) {
        try {
            int size = obj.size();
            output.writeInt(size, true);

            if (size == 0) {
                final Class<?> componentType = obj.toArray()
                                                  .getClass()
                                                  .getComponentType();
                kryo.writeClass(output, componentType);
            }
            else {
                kryo.writeClass(output,
                                obj.get(0)
                                   .getClass());
                for (final Object item : obj) {
                    kryo.writeClassAndObject(output, item);
                }
            }

        } catch (final RuntimeException e) {
            // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
            // handles SerializationException specifically (resizing the buffer)...
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public
    List<?> copy(Kryo kryo, List<?> original) {
        try {
            Object[] object = original.toArray();
            kryo.reference(object);
            Object[] arrayCopy = kryo.copy(object);
            return Arrays.asList(arrayCopy);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static
    Class<?> getPrimitiveWrapperClass(final Class<?> c) {
        if (c.isPrimitive()) {
            if (c.equals(Long.TYPE)) {
                return Long.class;
            }
            else if (c.equals(Integer.TYPE)) {
                return Integer.class;
            }
            else if (c.equals(Double.TYPE)) {
                return Double.class;
            }
            else if (c.equals(Float.TYPE)) {
                return Float.class;
            }
            else if (c.equals(Boolean.TYPE)) {
                return Boolean.class;
            }
            else if (c.equals(Character.TYPE)) {
                return Character.class;
            }
            else if (c.equals(Short.TYPE)) {
                return Short.class;
            }
            else if (c.equals(Byte.TYPE)) {
                return Byte.class;
            }
        }
        return c;
    }
}
