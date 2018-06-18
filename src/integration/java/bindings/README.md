- [libirohajava.jnilib](./libirohajava.jnilib), version: `0f9736109`, compiled for MAC OS. 


Use `otool -L libirohajava.jnilib` to see a list of its native dependencies.

@warchant:
```
libirohajava.jnilib:
        /usr/local/opt/protobuf/lib/libprotobuf.15.dylib (compatibility version 16.0.0, current version 16.1.0)
        @rpath/libed25519.1.3.0.dylib (compatibility version 1.3.0, current version 1.3.0)
        /usr/local/opt/boost/lib/libboost_filesystem-mt.dylib (compatibility version 0.0.0, current version 0.0.0)
        /usr/local/opt/boost/lib/libboost_system-mt.dylib (compatibility version 0.0.0, current version 0.0.0)
        /usr/lib/libc++.1.dylib (compatibility version 1.0.0, current version 400.9.0)
        /usr/lib/libSystem.B.dylib (compatibility version 1.0.0, current version 1252.50.4)

```

to use it you probably need to install `brew install grpc protobuf boost` and install [hyperledger/iroha-ed25519](https://github.com/hyperledger/iroha-ed25519).

If you have Linux, you should build library yourself [https://github.com/hyperledger/iroha/tree/master/example/java](https://github.com/hyperledger/iroha/tree/master/example/java)

