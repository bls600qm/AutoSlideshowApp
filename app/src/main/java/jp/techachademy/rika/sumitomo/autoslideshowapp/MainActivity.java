package jp.techachademy.rika.sumitomo.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;
import android.os.Handler;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v7.app.AlertDialog;

public class MainActivity extends AppCompatActivity  {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private Cursor mCursor;
    Timer mTimer;
    private Handler mHandler = new Handler();
    private Button button1;
    private Button button2;
    private Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonInit();

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:

                break;
        }
    }

    private void getContentsInfo() {


        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (mCursor.moveToFirst()) { //取得した結果に対するカーソルを先頭に移動させる という意味.成功したらtrueが返される.
            imageShow();

        }else{
            showAlertDialog();
            button1.setEnabled(false);
            button2.setEnabled(false);
            button3.setEnabled(false);
        }

    }



    private void imageShow() {

        int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = mCursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        Log.d("ANDROID", "URI : " + imageUri.toString());
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);

    }

    private void imageNext(){

        if(mCursor != null){
            if (mCursor.isLast()) {
                mCursor.moveToFirst();
            }else{
                mCursor.moveToNext();
            }
            imageShow();
        }
    }

    private void imageAutoNext(){

        if (mTimer != null) { //mTimerがnullでない
            //自動切り替え中

            mTimer.cancel();
            mTimer = null; // Timerは使い捨てなのでnullを入れて間違って参照されたりしないようにしてる//停止している状態
            button2.setText("再生する");
            button1.setEnabled(true);
            button3.setEnabled(true);

        }else{
            //自動切り替え中でない

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageNext(); //再生している状態

                        }
                    });
                }
            },2000,2000);
            button2.setText("停止する");
            button1.setEnabled(false);
            button3.setEnabled(false);

        }

    }

    private void imagePrev(){
        if(mCursor !=null){
            if (mCursor.isFirst()) {
                mCursor.moveToLast();
            }else{
                mCursor.moveToPrevious();
            }
            imageShow();
        }
    }


    private void buttonInit(){

        button1 = (Button)findViewById(R.id.go);
        button2 = (Button)findViewById(R.id.stop);
        button3 = (Button)findViewById(R.id.back);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("GO", "進むボタンをタップしました");
                imageNext();
            }
        });
        button2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("STOP","再生/一時停止ボタンをタップしました");
                imageAutoNext();
            }
        });
        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("BACK","戻るボタンをタップしました");
                imagePrev();

            }
        });

    }
    private void showAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("エラー");
        alertDialogBuilder.setMessage("画像がありません");

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(mCursor != null){
            mCursor.close();
        }
    }


}

