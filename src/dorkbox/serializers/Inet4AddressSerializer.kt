package dorkbox.serializers

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.net.Inet4Address
import java.net.InetAddress

class Inet4AddressSerializer : Serializer<Inet4Address>() {
    init {
        isImmutable = true
    }

    override fun write(kryo: Kryo, output: Output, inetAddress: Inet4Address) {
        output.writeBytes(inetAddress.address)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Inet4Address>): Inet4Address {
        return InetAddress.getByAddress(null, input.readBytes(4)) as Inet4Address
    }
}
