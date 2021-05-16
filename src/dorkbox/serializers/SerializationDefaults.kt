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
package dorkbox.serializers

import com.esotericsoftware.kryo.Kryo
import java.io.IOException
import java.math.BigDecimal
import java.net.URI
import java.util.*
import java.util.regex.Pattern

object SerializationDefaults {
    /**
     * Gets the version number.
     */
    const val version = "1.2"

    /**
     * Allows for the kryo registration of sensible defaults in a common, well-used way.
     */
    fun register(kryo: Kryo) {
        // these are registered using the default serializers. We don't customize these, because we don't care about it.
        kryo.register(String::class.java)
        kryo.register(Array<String>::class.java)
        kryo.register(IntArray::class.java)
        kryo.register(ShortArray::class.java)
        kryo.register(FloatArray::class.java)
        kryo.register(DoubleArray::class.java)
        kryo.register(LongArray::class.java)
        kryo.register(ByteArray::class.java)
        kryo.register(CharArray::class.java)
        kryo.register(BooleanArray::class.java)
        kryo.register(Array<Int>::class.java)
        kryo.register(Array<Short>::class.java)
        kryo.register(Array<Float>::class.java)
        kryo.register(Array<Double>::class.java)
        kryo.register(Array<Long>::class.java)
        kryo.register(Array<Byte>::class.java)
        kryo.register(Array<Char>::class.java)
        kryo.register(Array<Boolean>::class.java)
        kryo.register(Array<Any>::class.java)
        kryo.register(Array<Array<Any>>::class.java)
        kryo.register(Class::class.java)
        kryo.register(Exception::class.java)
        kryo.register(IOException::class.java)
        kryo.register(RuntimeException::class.java)
        kryo.register(NullPointerException::class.java)

        kryo.register(BigDecimal::class.java)
        kryo.register(BitSet::class.java)

        // necessary for the transport of exceptions.
        kryo.register(StackTraceElement::class.java)
        kryo.register(Array<StackTraceElement>::class.java)
        kryo.register(ArrayList::class.java)
        kryo.register(HashMap::class.java)
        kryo.register(HashSet::class.java)

        kryo.register(EnumSet::class.java, EnumSetSerializer())
        kryo.register(EnumMap::class.java, EnumMapSerializer())
        kryo.register(Arrays.asList("").javaClass, ArraysAsListSerializer())

        kryo.register(emptyList<Any>().javaClass)
        kryo.register(emptySet<Any>().javaClass)
        kryo.register(emptyMap<Any, Any>().javaClass)
        kryo.register(Collections.EMPTY_LIST.javaClass)
        kryo.register(Collections.EMPTY_SET.javaClass)
        kryo.register(Collections.EMPTY_MAP.javaClass)

        kryo.register(Collections.emptyNavigableSet<Any>().javaClass)
        kryo.register(Collections.emptyNavigableMap<Any, Any>().javaClass)

        kryo.register(Collections.singletonMap("", "").javaClass, CollectionsSingletonMapSerializer())
        kryo.register(listOf("").javaClass, CollectionsSingletonListSerializer())
        kryo.register(setOf("").javaClass, CollectionsSingletonSetSerializer())

        kryo.register(Pattern::class.java, RegexSerializer())
        kryo.register(URI::class.java, URISerializer())
        kryo.register(UUID::class.java, UUIDSerializer())

        UnmodifiableCollectionsSerializer.registerSerializers(kryo)
        SynchronizedCollectionsSerializer.registerSerializers(kryo)
    }
}
