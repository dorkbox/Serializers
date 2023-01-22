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
import com.esotericsoftware.kryo.Registration
import com.esotericsoftware.kryo.Serializer
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.URI
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern

@Suppress("MemberVisibilityCanBePrivate")
object SerializationDefaults {
    /**
     * Gets the version number.
     */
    const val version = "2.8"

    init {
        // Add this project to the updates system, which verifies this class + UUID + version information
        dorkbox.updates.Updates.add(SerializationDefaults::class.java, "316353f5338341a8a3edc01d702703f8", version)
    }

    val regexSerializer by lazy { RegexSerializer() }
    val uriSerializer by lazy { URISerializer() }
    val uuidSerializer by lazy { UUIDSerializer() }

    val collectionsSingletonMapSerializer by lazy { CollectionsSingletonMapSerializer() }
    val collectionsSingletonListSerializer by lazy { CollectionsSingletonListSerializer() }
    val collectionsSingletonSetSerializer by lazy { CollectionsSingletonSetSerializer() }

    val enumSetSerializer by lazy { EnumSetSerializer() }
    val enumMapSerializer by lazy { EnumMapSerializer() }
    val arraysAsListSerializer by lazy { ArraysAsListSerializer() }

    val inet4AddressSerializer by lazy { Inet4AddressSerializer() }
    val inet6AddressSerializer by lazy { Inet6AddressSerializer() }
    val fileSerializer by lazy { FileSerializer() }

    /**
     * Allows for the kryo registration of sensible defaults in a common, well-used way. Uses a hashmap to return registration data
     */
    fun register(): MutableMap<Class<*>, Serializer<*>?> {
        val registeredClasses: MutableMap<Class<*>, Serializer<*>?> = mutableMapOf()
        val kryo = object : Kryo() {
            override fun register(type: Class<*>): Registration {
                registeredClasses[type] = null
                return super.register(type)
            }

            override fun register(type: Class<*>, serializer: Serializer<*>?): Registration {
                registeredClasses[type] = serializer
                return super.register(type, serializer)
            }
        }

        register(kryo)
        return registeredClasses
    }

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

        kryo.register(EnumSet::class.java, enumSetSerializer)
        kryo.register(EnumMap::class.java, enumMapSerializer)
        kryo.register(Arrays.asList("").javaClass, arraysAsListSerializer)

        kryo.register(emptyList<Any>().javaClass)
        kryo.register(emptySet<Any>().javaClass)
        kryo.register(emptyMap<Any, Any>().javaClass)
        kryo.register(Collections.EMPTY_LIST.javaClass)
        kryo.register(Collections.EMPTY_SET.javaClass)
        kryo.register(Collections.EMPTY_MAP.javaClass)

        kryo.register(Collections.emptyNavigableSet<Any>().javaClass)
        kryo.register(Collections.emptyNavigableMap<Any, Any>().javaClass)

        kryo.register(Collections.singletonMap("", "").javaClass, collectionsSingletonMapSerializer)
        kryo.register(listOf("").javaClass, collectionsSingletonListSerializer)
        kryo.register(setOf("").javaClass, collectionsSingletonSetSerializer)

        kryo.register(Pattern::class.java, regexSerializer)
        kryo.register(URI::class.java, uriSerializer)
        kryo.register(UUID::class.java, uuidSerializer)

        kryo.register(Inet4Address::class.java, inet4AddressSerializer)
        kryo.register(Inet6Address::class.java, inet6AddressSerializer)
        kryo.register(File::class.java, fileSerializer)

        UnmodifiableCollectionsSerializer.registerSerializers(kryo)
        SynchronizedCollectionsSerializer.registerSerializers(kryo)
    }
}
