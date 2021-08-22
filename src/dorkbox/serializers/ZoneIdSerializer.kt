package dorkbox.serializers

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.time.ZoneId

class ZoneIdSerializer: Serializer<ZoneId>() {
    override fun write(kryo: Kryo, output: Output, zoneId: ZoneId) {
        output.writeString(zoneId.id)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out ZoneId>): ZoneId {
        return ZoneId.of(input.readString())
    }
}
