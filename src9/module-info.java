module dorkbox.serializers {
    exports dorkbox.serializers;
    exports dorkbox.serializers.bouncycastle;

    requires com.esotericsoftware.kryo;
    requires com.esotericsoftware.reflectasm;

    requires static org.bouncycastle.provider;

    requires kotlin.stdlib;

    requires java.base;
}
