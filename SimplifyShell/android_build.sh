#!/bin/bash
      #ndk路径
      NDK=//Users/dev/Downloads/android-ndk-r10e
      #版本号
      SYSROOT=$NDK/platforms/android-16/arch-arm/
      TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64
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
      --enable-filter=scale \
      --enable-parser=png \
      --enable-cross-compile \
      --disable-asm \
      --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
      --target-os=linux \
      --arch=arm \
      --sysroot=$SYSROOT \
      --extra-cflags="-Os -fpic $ADDI_CFLAGS" \
      --extra-ldflags="$ADDI_LDFLAGS" \
      $ADDITIONAL_CONFIGURE_FLAG
      }
      CPU=arm
      PREFIX=$(pwd)/android/$CPU
      ADDI_CFLAGS=""
      build_one
      make
      make install