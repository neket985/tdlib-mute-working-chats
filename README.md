# Инструкция по сборке

Собираем библиотеку TDLib 
```
export JAVA_HOME=/Users/smirnov/Library/Java/JavaVirtualMachines/openjdk-21/Contents/Home
rm -rf /Users/smirnov/IdeaProjects/myself/td/jnibuild
cd /Users/smirnov/IdeaProjects/myself/td
mkdir jnibuild && cd jnibuild
cmake -DCMAKE_BUILD_TYPE=Release -DTD_ENABLE_JNI=ON -DCMAKE_INSTALL_PREFIX:PATH=/Users/smirnov/IdeaProjects/myself/tdlib-mute-working-chats/td ..
cmake --build . --target install
```

Собираем текущий проект
```
rm -rf /Users/smirnov/IdeaProjects/myself/tdlib-mute-working-chats/build
cd /Users/smirnov/IdeaProjects/myself/tdlib-mute-working-chats
mkdir build && cd build
cmake -DCMAKE_BUILD_TYPE=Release -DTd_DIR=/Users/smirnov/IdeaProjects/myself/tdlib-mute-working-chats/td/lib/cmake/Td -DCMAKE_INSTALL_PREFIX:PATH=.. ..
cmake --build . --target install
mv libtdjni.dylib ../lib
```
 

------------------------------------------------------------------------------------------------------------------------
# TDLib Java example

To run this example, you will need installed JDK >= 1.6.
For Javadoc documentation generation PHP is needed.

You can find complete build instructions for your operating system at https://tdlib.github.io/td/build.html?language=Java.

In general, the build process looks as follows.

TDLib should be prebuilt with JNI bindings and installed to local subdirectory `td/` as follows:
```
cd /Users/smirnov/IdeaProjects/myself/td
mkdir jnibuild
cd jnibuild
cmake -DCMAKE_BUILD_TYPE=Release -DTD_ENABLE_JNI=ON -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td ..
cmake --build . --target install
```
If you want to compile TDLib for 32-bit/64-bit Java on Windows using MSVC, you will also need to add `-A Win32`/`-A x64` option to CMake.

In Windows, use vcpkg toolchain file by adding parameter -DCMAKE_TOOLCHAIN_FILE=<VCPKG_DIR>/scripts/buildsystems/vcpkg.cmake

After this you can compile the example source code:
```
cd /Users/smirnov/IdeaProjects/myself/td/example/java
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -DTd_DIR=/Users/smirnov/IdeaProjects/myself/td/example/java/td/lib/cmake/Td -DCMAKE_INSTALL_PREFIX:PATH=.. ..
cmake --build . --target install
```

Compiled TDLib shared library and Java example after that will be placed in bin/ and Javadoc documentation in `docs/`.

After this you can run the Java example:
```
cd /Users/smirnov/IdeaProjects/myself/td/example/java/bin
java '-Djava.library.path=.' org/drinkless/tdlib/example/Example
```

If you receive "Could NOT find JNI ..." error from CMake, you need to specify to CMake path to the installed JDK, for example, "-DJAVA_HOME=/usr/lib/jvm/java-8-oracle/".

If you receive java.lang.UnsatisfiedLinkError with "Can't find dependent libraries", you may also need to copy some dependent shared OpenSSL and zlib libraries to `bin/`.

Make sure that you compiled the example for the same architecture as your JVM.
