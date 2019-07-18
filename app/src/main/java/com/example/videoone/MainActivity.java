package com.example.videoone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static final int PHOTO = 123;
    private static final int VIDEO = 124;
    private Context context;


    ImageView img_view;
    TextView tv;


    String video_path;
    private File file;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        int checkResult = context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //if(!=允许),抛出异常
        if (checkResult != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1); // 动态申请读取权限
        } else {
        }

        init_val();


    }

    private void init_val() {

        img_view = findViewById(R.id.imageView);
        tv = findViewById(R.id.textView);


        init();
    }

    private void init() {



    }

    public void choose_video(View view) {
        // 进入相册 以下是例子：用不到的api可以不写
        PictureSelector.create(MainActivity.this)
                .openGallery(PictureMimeType.ofVideo())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageSpanCount(4)// 每行显示个数 int
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .previewVideo(true)// 是否可预览视频 true or false
                .isCamera(true)// 是否显示拍照按钮 true or false
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult c
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {


                try {
                    // 图片、视频、音频选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    video_path = selectList.get(0).getPath();
                    setTitle("当前视频：" + new File(video_path).getName());

                } catch (Exception e) {
                    Toast.makeText(context, "视频选择异常:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }


                try {
                    get_one();
                } catch (Exception e) {
                    Toast.makeText(context, "获取第一帧异常:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    private void get_one() throws Exception {
        //获取第一帧

        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(video_path);
        Bitmap bitmap = media.getFrameAtTime();

        img_view.setImageBitmap(bitmap);

        img_view.setVisibility(View.VISIBLE);
        tv.setVisibility(View.GONE);

    }



    //根据view获取bitmap
    public static Bitmap getBitmapByView(View view) {
        int h = 0;
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    //检查sd
    public static boolean checkSDCardAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static void savePhotoToSDCard(Bitmap photoBitmap, String path, String photoName) {
        if (checkSDCardAvailable()) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName + ".png");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void save_one(View view) {

        if (!getTitle().equals("视频第一帧")) {
            Bitmap bitmap = getBitmapByView(img_view);//iv是View
            int ran = new Random().nextInt(1000);
            savePhotoToSDCard(bitmap, "/sdcard/视频第一帧", "video_one_" + ran);
            file = new File("/sdcard/视频第一帧/video_one_" + ran + ".png");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "请先选择一个视频", Toast.LENGTH_SHORT).show();
        }

    }

    public void set_one(View view) {

        if (!getTitle().equals("视频第一帧")) {


        } else {
            Toast.makeText(context, "请先选择一个视频", Toast.LENGTH_SHORT).show();
        }

    }
}
