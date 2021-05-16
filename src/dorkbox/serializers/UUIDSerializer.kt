package dorkbox.serializers

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.util.*

class UUIDSerializer : Serializer<UUID>() {
    init {
        isImmutable = true
    }

    override fun write(kryo: Kryo, output: Output, uuid: UUID) {
        output.writeLong(uuid.mostSignificantBits)
        output.writeLong(uuid.leastSignificantBits)
    }

    override fun read(kryo: Kryo, input: Input, uuidClass: Class<out UUID>): UUID {
        return UUID(input.readLong(), input.readLong())
    }
}
