package dorkbox.serializers

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.net.Inet6Address
import java.net.InetAddress

class Inet6AddressSerializer : Serializer<Inet6Address>() {
    init {
        isImmutable = true
    }

    override fun write(kryo: Kryo, output: Output, inetAddress: Inet6Address) {
        output.writeBytes(inetAddress.address) // 16 bytes
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Inet6Address>): Inet6Address {
        return InetAddress.getByAddress(input.readBytes(16)) as Inet6Address
    }
}
