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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.esotericsoftware.kryo.Kryo;

public
class SerializationDefaults {
    /**
     * Allows for the kryo registration of sensible defaults in a common, well used way.
     */
    public static
    void register(Kryo kryo) {
        // these are registered using the default serializers. We don't customize these, because we don't care about it.
        kryo.register(String.class);
        kryo.register(String[].class);

        kryo.register(int[].class);
        kryo.register(short[].class);
        kryo.register(float[].class);
        kryo.register(double[].class);
        kryo.register(long[].class);
        kryo.register(byte[].class);
        kryo.register(char[].class);
        kryo.register(boolean[].class);

        kryo.register(Integer[].class);
        kryo.register(Short[].class);
        kryo.register(Float[].class);
        kryo.register(Double[].class);
        kryo.register(Long[].class);
        kryo.register(Byte[].class);
        kryo.register(Character[].class);
        kryo.register(Boolean[].class);

        kryo.register(Object[].class);
        kryo.register(Object[][].class);
        kryo.register(Class.class);

        kryo.register(Exception.class);
        kryo.register(IOException.class);
        kryo.register(RuntimeException.class);
        kryo.register(NullPointerException.class);

        // necessary for the transport of exceptions.
        kryo.register(StackTraceElement.class);
        kryo.register(StackTraceElement[].class);

        kryo.register(ArrayList.class);
        kryo.register(HashMap.class);
        kryo.register(HashSet.class);

        kryo.register(Collections.emptyList().getClass());
        kryo.register(Collections.emptySet().getClass());
        kryo.register(Collections.emptyMap().getClass());

        kryo.register(Collections.emptyNavigableSet().getClass());
        kryo.register(Collections.emptyNavigableMap().getClass());

        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
    }
}
