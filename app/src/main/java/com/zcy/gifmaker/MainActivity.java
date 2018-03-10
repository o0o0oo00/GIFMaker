package com.zcy.gifmaker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import com.jni.FFmpegJni;

import static com.zhihu.matisse.ui.MatisseActivity.MULTICHOICE;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    public static final int REQUEST_CODE_CHOOSE = 23;
    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "gif.gif";
    String res = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "GIFMaker" + File.separator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.gifmaker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                if (aBoolean) {
                                    switch (v.getId()) {
                                        case R.id.gifmaker:
                                            Matisse.from(MainActivity.this)
                                                    .choose(MimeType.of(MimeType.PNG, MimeType.JPEG), false)
                                                    .countable(true)
                                                    .maxSelectable(9)
                                                    .thumbnailScale(0.85f)
                                                    .imageEngine(new GlideEngine())
                                                    .forResult(REQUEST_CODE_CHOOSE, MULTICHOICE);
                                            break;


                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    makeGif(Matisse.obtainResult(data));
                }
            }).start();


        }
    }

    /**
     * 通过一组Uri制作GIF
     */
    private void makeGif(List<Uri> uris) {
        final ImageView imageView = findViewById(R.id.image_view);
        double finalW = 0;
        double finalH = 0;

        double[] scales = new double[uris.size()];

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        String sourcePath = null;
        for (int i = 0; i < uris.size(); i++) {
            sourcePath = getRealPathFromURI(MainActivity.this, uris.get(i));
            BitmapFactory.decodeFile(sourcePath, op);
            double ow = op.outWidth;
            double oh = op.outHeight;
            scales[i] = (ow / oh);

        }

        double finalScale = getMostAppear(scales);

        if (finalScale < 1) {//高比较大
            finalH = 1200;
            finalW = (finalScale * finalH);
        } else {
            finalW = 1200;
            finalH = (finalW / finalScale);

        }

        int width = (int) finalW;
        int height = (int) finalH;

        for (int i = 0; i < uris.size(); i++) {
            Bitmap bitmap;
            sourcePath = getRealPathFromURI(MainActivity.this, uris.get(i));
            if (TextUtils.isEmpty(sourcePath)) {
                continue;
            }
            bitmap = BitmapFactory.decodeFile(sourcePath);
            Log.e("makeGif: ", "init i = " + i + " w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
            double bScale = bitmap.getWidth() / bitmap.getHeight();
            double w = bitmap.getWidth() + 1;
            double h = bitmap.getHeight();

            double dstHeight = h / (w / width);
            double dstWidth = w / (h / height);

            if (bScale < finalScale) {//比最终高、缩放宽度、裁剪高度
                bitmap = Bitmap.createScaledBitmap(bitmap, width, (int) dstHeight, true);
                Log.e("makeGif: ", "i1 = " + i + " w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
                bitmap = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() - height) / 2, width, height);
                Log.e("makeGif: ", "i2 = " + i + " w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
            } else if (bScale > finalScale) {//比最终宽、缩放高度、裁剪宽度
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) dstWidth, height, true);
                Log.e("makeGif: ", "i1 = " + i + " w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
                bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - width) / 2, 0, width, height);
                Log.e("makeGif: ", "i2 = " + i + " w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
            } else {
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                Log.e("makeGif: ", "i = " + i + " w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
            }

            saveBitmap(bitmap, i);

        }
        String[] commend = FFmpegCommands.photos2Gif(res, path);

        System.out.println(commend.toString());

        FFmpegJni.run(commend);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "已保存gif到" + path, Toast.LENGTH_LONG).show();

                Glide.with(MainActivity.this).load(path).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageView);

                deleteFiles(res);

            }
        });


    }

    private double getMostAppear(double[] arr) {
        double most = arr[0];
        int count = 1;
        int cc = count;

        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] == arr[j]) {
                    count++;
                }
            }

            if (cc < count) {
                cc = count;
                most = arr[i];
            }
            count = 1;

        }

        return most;
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public void saveBitmap(Bitmap bitmap, int num) {
        try {
            File file = new File(res + "temp0" + (num + 1) + ".jpg");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            System.out.println("保存成功" + file.getAbsolutePath());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteFiles(String dir) {
        File file = new File(dir);
        if (file.exists()) {
            file.delete();
        }
    }
}
