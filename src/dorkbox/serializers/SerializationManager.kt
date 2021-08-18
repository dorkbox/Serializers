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

import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.io.IOException

interface SerializationManager<IO> {
    /**
     * Registers the class using the lowest, next available integer ID and the [default serializer][Kryo.getDefaultSerializer].
     * If the class is already registered, the existing entry is updated with the new serializer.
     *
     *
     * Registering a primitive also affects the corresponding primitive wrapper.
     *
     *
     * Because the ID assigned is affected by the IDs registered before it, the order classes are registered is important when using this
     * method. The order must be the same at deserialization as it was for serialization.
     */
    fun <T> register(clazz: Class<T>): SerializationManager<*>

    /**
     * Registers the class using the specified ID. If the ID is already in use by the same type, the old entry is overwritten. If the ID
     * is already in use by a different type, a [KryoException] is thrown.
     *
     *
     * Registering a primitive also affects the corresponding primitive wrapper.
     *
     *
     * IDs must be the same at deserialization as they were for serialization.
     *
     * @param id Must be >= 0. Smaller IDs are serialized more efficiently. IDs 0-8 are used by default for primitive types and String, but
     * these IDs can be repurposed.
     */
    fun <T> register(clazz: Class<T>, id: Int): SerializationManager<*>

    /**
     * Registers the class using the lowest, next available integer ID and the specified serializer. If the class is already registered,
     * the existing entry is updated with the new serializer.
     *
     *
     * Registering a primitive also affects the corresponding primitive wrapper.
     *
     *
     * Because the ID assigned is affected by the IDs registered before it, the order classes are registered is important when using this
     * method. The order must be the same at deserialization as it was for serialization.
     */
    fun <T> register(clazz: Class<T>, serializer: Serializer<T>): SerializationManager<*>

    /**
     * Registers the class using the specified ID and serializer. If the ID is already in use by the same type, the old entry is
     * overwritten. If the ID is already in use by a different type, a [KryoException] is thrown.
     *
     *
     * Registering a primitive also affects the corresponding primitive wrapper.
     *
     *
     * IDs must be the same at deserialization as they were for serialization.
     *
     * @param id Must be >= 0. Smaller IDs are serialized more efficiently. IDs 0-8 are used by default for primitive types and String, but
     * these IDs can be repurposed.
     */
    fun <T> register(clazz: Class<T>, serializer: Serializer<T>, id: Int): SerializationManager<*>

    /**
     * Waits until a kryo is available to write, using CAS operations to prevent having to synchronize.
     *
     * There is a small speed penalty if there were no kryo's available to use.
     */
    @Throws(IOException::class)
    fun write(buffer: IO, message: Any?)

    /**
     * Reads an object from the buffer.
     *
     * @param length should ALWAYS be the length of the expected object!
     */
    @Throws(IOException::class)
    fun read(buffer: IO, length: Int): Any?

    /**
     * Writes the class and object using an available kryo instance
     */
    @Throws(IOException::class)
    fun writeFullClassAndObject(output: Output, value: Any?)

    /**
     * Returns a class read from the input
     */
    @Throws(IOException::class)
    fun readFullClassAndObject(input: Input): Any?
}
