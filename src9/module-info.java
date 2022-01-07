module dorkbox.serializers {
    exports dorkbox.serializers;
    exports dorkbox.serializers.bouncycastle;

    requires transitive dorkbox.updates;

    requires transitive com.esotericsoftware.kryo;
    requires transitive com.esotericsoftware.reflectasm;

    requires static org.bouncycastle.provider;

    requires transitive kotlin.stdlib;
}
