package com.gizwits.opensource.appkit.extra.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.gizwits.opensource.appkit.extra.ui.entity.MusicFile;
import com.gizwits.opensource.appkit.extra.ui.utils.Utils;
import com.gizwits.opensource.gokit.R;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 音乐选择界面
 * Created by zxd on 17-4-10.
 */
@RuntimePermissions
public class SelectMusicActivity extends Activity {
    private final static int MUSIC_SCARCH_FINISH = 1;//音乐扫描完成
    private final static int MUSIC_SELECTED = 2; //选择了一首音乐

    private int mPosition = -1;//选择音乐的编号
    private ProgressDialog mProgressDialog;
    private List<MusicFile> mMusicFiles = new ArrayList<MusicFile>();
    private MyAdapter myAdapter;

    private ListView mListMusic;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MUSIC_SCARCH_FINISH://音乐扫描完成
                    mProgressDialog.dismiss();
                    mListMusic.setAdapter(myAdapter = new MyAdapter());
                    break;
                case MUSIC_SELECTED:
                    final MusicFile musicFile = mMusicFiles.get(mPosition);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SelectMusicActivity.this);
                    builder.setMessage(musicFile.getName());
                    builder.setTitle("确认选择？");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(SelectMusicActivity.this, MusicLightActivity.class);
                            intent.putExtra("music", musicFile.getDir());
                            startActivity(intent);
                            SelectMusicActivity.this.finish();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    });
                    builder.create().show();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_music);
        mListMusic = (ListView) findViewById(R.id.list_music);

        SelectMusicActivityPermissionsDispatcher.getMusicFileWithCheck(this);
    }

    /**
     * 利用ContentProvider扫描手机中的音乐,此方法在运行在子线程中完成音乐的扫描,获取所有音乐文件
     */
    public void getMusicFloder() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "没有sdcard", Toast.LENGTH_LONG).show();
            return;
        }
        // 显示进度条
        mProgressDialog = ProgressDialog.show(this, null, "正在加载中");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri mImageUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = SelectMusicActivity.this.getContentResolver();
                String selection = MediaStore.Audio.Media.MIME_TYPE + "=? ";
                String[] selectionArgs = new String[]{"audio/mpeg"};
                Cursor mCursor = mContentResolver.query(mImageUri, null, selection, selectionArgs, MediaStore.Audio.Media.DATE_MODIFIED);//危险权限
                assert mCursor != null;
                Log.i("zzz", "扫描完成");
                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    int duration = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    MusicFile musicFile = new MusicFile();
                    musicFile.setDir(path);
                    musicFile.setMusicDuration(duration);
                    mMusicFiles.add(musicFile);
                }
                mCursor.close();
                // 通知Handler扫描图片完成
                mHandler.sendEmptyMessage(MUSIC_SCARCH_FINISH);
            }
        }).start();
    }

    //-------------------------私有类   私有方法---------------------
    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mMusicFiles.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mMusicFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (null == convertView) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(SelectMusicActivity.this).inflate(R.layout.item_music_file, parent, false);
                holder.musicInfo = (LinearLayout) convertView.findViewById(R.id.music_info);
                holder.musicFolderName = (TextView) convertView.findViewById(R.id.music_folder_name);
                holder.musicDuration = (TextView) convertView.findViewById(R.id.music_duration);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MusicFile musicFile = mMusicFiles.get(position);
            holder.musicFolderName.setText(musicFile.getName().substring(1));
            holder.musicDuration.setText(Utils.getMusicDuration(musicFile.getMusicDuration()));

            holder.musicInfo.setBackgroundColor(getResources().getColor(R.color.white));

            holder.musicInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    itemClick(holder, position);
                    mPosition = position;
                    mHandler.sendEmptyMessage(MUSIC_SELECTED);
                }
            });
            return convertView;
        }
    }

    private class ViewHolder {
        private LinearLayout musicInfo;
        private TextView musicFolderName;
        private TextView musicDuration;
    }

    @NeedsPermission(android.Manifest.permission.RECORD_AUDIO)
    void getMusicFile() {
        getMusicFloder();//查询本地所有音乐
    }

    @OnShowRationale(android.Manifest.permission.RECORD_AUDIO)
    void showRationaleForRecord(final PermissionRequest request) {
        showRationaleDialog(R.string.permission_record_rationale, request);
    }

    @OnPermissionDenied(android.Manifest.permission.RECORD_AUDIO)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.permission_record_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(android.Manifest.permission.RECORD_AUDIO)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.permission_record_neverask, Toast.LENGTH_SHORT).show();
    }

    private void showRationaleDialog(@StringRes int messageRestId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageRestId)
                .show();
    }
}
