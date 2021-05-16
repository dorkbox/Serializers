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

import java.util.EnumMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A serializer for {@link EnumMap}s.
 *
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public
class EnumMapSerializer extends Serializer<EnumMap<? extends Enum<?>, ?>> {

    static {
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public
    EnumMap<? extends Enum<?>, ?> copy(final Kryo kryo, final EnumMap<? extends Enum<?>, ?> original) {
        // Make a shallow copy to copy the private key type of the original map without using reflection.
        // This will work for empty original maps as well.
        final EnumMap copy = new EnumMap(original);
        for (final Map.Entry entry : original.entrySet()) {
            copy.put((Enum) entry.getKey(), kryo.copy(entry.getValue()));
        }
        return copy;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public
    EnumMap<? extends Enum<?>, ?> read(final Kryo kryo, final Input input, final Class<? extends EnumMap<? extends Enum<?>, ?>> type) {
        final Class<? extends Enum<?>> keyType = kryo.readClass(input)
                                                     .getType();
        final EnumMap<? extends Enum<?>, ?> result = new EnumMap(keyType);
        final Enum<?>[] enumConstants = keyType.getEnumConstants();
        final EnumMap rawResult = result;
        final int size = input.readInt(true);
        for (int i = 0; i < size; i++) {
            final int ordinal = input.readVarInt(true);
            final Enum<?> key = enumConstants[ordinal];
            final Object value = kryo.readClassAndObject(input);
            rawResult.put(key, value);
        }
        return result;
    }

    @Override
    public
    void write(final Kryo kryo, final Output output, final EnumMap<? extends Enum<?>, ?> map) {
        if (map.isEmpty()) {
            throw new KryoException("An EnumMap must not be empty to be serialized, the type can not be safely inferred.");
        } else {
            @SuppressWarnings("unchecked")
            Class<Enum<?>> keyType = (Class<Enum<?>>) map.keySet()
                                                         .iterator()
                                                         .next()
                                                         .getDeclaringClass();
            kryo.writeClass(output, keyType);
            output.writeInt(map.size(), true);
            for (final Map.Entry<? extends Enum<?>, ?> entry : map.entrySet()) {
                output.writeVarInt(entry.getKey()
                                        .ordinal(), true);
                kryo.writeClassAndObject(output, entry.getValue());
            }
        }
    }
}
