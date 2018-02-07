package com.zcy.gifmaker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lchad.gifflen.Gifflen;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.zhihu.matisse.ui.MatisseActivity.GALLERYCHOICE;
import static com.zhihu.matisse.ui.MatisseActivity.MULTICHOICE;
import static com.zhihu.matisse.ui.MatisseActivity.SINGLECHOICE;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    public static final int REQUEST_CODE_CHOOSE = 23;

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
                                                    .choose(MimeType.ofAll(), false)
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
                    Toast.makeText(MainActivity.this, "开始制作", Toast.LENGTH_SHORT).show();
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
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "gifflen-sample1.gif";
        double finalW = 0;
        double finalH = 0;

        double[] scales = new double[uris.size()];

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;

        for (int i = 0; i < uris.size(); i++) {
            String sourcePath = getRealPathFromURI(MainActivity.this, uris.get(i));
            BitmapFactory.decodeFile(sourcePath, op);
            double ow = op.outWidth;
            double oh = op.outHeight;
            scales[i] = (ow / oh);
            if (i == 0) {
                finalH = op.outHeight;
                finalW = op.outWidth;
            }

        }

        double finalScale = getMostAppear(scales);

        if (finalScale < 1) {//高比较大
            finalH = 1200;
            finalW = (finalScale * finalH);
        } else {
            finalW = 1200;
            finalH = (finalW / finalScale);

        }

        Gifflen gifflen = new Gifflen.Builder()
                .color(256)
                .delay(1000)
                .quality(10)
                .width((int) finalW)
                .height((int) finalH)
                .listener(new Gifflen.OnEncodeFinishListener() {
                    @Override
                    public void onEncodeFinish(String path) {
                        Toast.makeText(MainActivity.this, "已保存gif到" + path, Toast.LENGTH_LONG).show();
                        Glide.with(MainActivity.this).load(path).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageView);
                    }
                })
                .build();


        gifflen.encode(MainActivity.this, path, uris);
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
}
