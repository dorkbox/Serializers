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
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.nio.file.Path
import java.nio.file.Paths


/**
 * Serialize the path of a file instead of the Path object
 */
class PathSerializer : Serializer<Path>() {
    init {
        isImmutable = true
    }

    override fun write(kryo: Kryo, output: Output, path: Path) {
        output.writeString(path.toString())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Path>): Path {
        val path = input.readString()
        return Paths.get(path)
    }
}
