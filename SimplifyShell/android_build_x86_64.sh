#!/bin/bash
      #ndk路径
      NDK=//Users/dev/Downloads/android-ndk-r10e
      #版本号
      SYSROOT=$NDK/platforms/android-21/arch-x86_64/
      TOOLCHAIN=$NDK/toolchains/x86_64-4.9/prebuilt/darwin-x86_64
      function build_one(){
      ./configure \
      --prefix=$PREFIX \
      --disable-static \
      --disable-doc \
      --disable-ffserver \
      --disable-everything \
      --enable-shared \
      --enable-decoder=mpeg4 \
      --enable-encoder=mpeg4 \
      --enable-decoder=gif \
      --enable-encoder=gif \
      --enable-encoder=mjpeg \
      --enable-decoder=mjpeg \
      --enable-encoder=png \
      --enable-ffmpeg \
      --enable-protocol=file \
      --enable-demuxer=mov \
      --enable-demuxer=mjpeg \
      --enable-demuxer=png \
      --enable-demuxer=image2 \
      --enable-muxer=mp4 \
      --enable-muxer=gif \
      --enable-muxer=mjpeg \
      --enable-muxer=image2 \
      --enable-parser=mpeg4video \
      --enable-parser=mjpeg \
      --enable-parser=png \
      --enable-filter=scale \
      --enable-cross-compile \
      --disable-asm \
      --cross-prefix=$TOOLCHAIN/bin/x86_64-linux-android- \
      --cc=$TOOLCHAIN/bin/x86_64-linux-android-gcc \
      --nm=$TOOLCHAIN/bin/x86_64-linux-android-nm \
      --target-os=linux \
      --arch=x86_64 \
      --sysroot=$SYSROOT \
      --extra-cflags="-Os -fpic $ADDI_CFLAGS" \
      --extra-ldflags="$ADDI_LDFLAGS" \
      $ADDITIONAL_CONFIGURE_FLAG
      }
      CPU=x86_64
      PREFIX=$(pwd)/android/$CPU
      ADDI_CFLAGS=""
      build_one
      make
      make install