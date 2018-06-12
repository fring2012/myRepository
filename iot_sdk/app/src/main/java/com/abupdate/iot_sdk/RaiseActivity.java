package com.abupdate.iot_sdk;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abupdate.iot_download_libs.DLManager;
import com.abupdate.iot_download_libs.DownEntity;
import com.abupdate.iot_download_libs.IOnDownListener;
import com.abupdate.trace.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RaiseActivity extends Activity {

    private static final String TAG = "RaiseActivity";
    private DLManager m_dlManager;

    private ProgressBar m_progressBar;
    private TextView m_textView;
    private ListView m_list_view;

    public List<DownEntity> m_downEntities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raise);

//        m_list_view = (ListView) findViewById(R.id.list_view);
        m_progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        m_textView = (TextView) findViewById(R.id.textview_notice);

//        m_list_view.setAdapter();

        m_dlManager = DLManager.getInstance();
        String folder = getFilesDir().getParentFile().getAbsolutePath() + "/download";

        DownEntity downEntity1 = new DownEntity("http://imtt.dd.qq.com/16891/8C3E058EAFBFD4F1EFE0AAA815250716.apk?fsname=com.tencent.mobileqq_7.1.0_692.apk&csr=1bbd",
                new File(folder, "qq.apk").getAbsolutePath()
//                ,
//                "8C3E058EAFBFD4F1EFE0AAA815250716",
//                44_906_057
        );
        DownEntity downEntity2 = new DownEntity("http://imtt.dd.qq.com/16891/D5E356B578B18A612A465DD27B6FA8BB.apk?fsname=com.tencent.tmgp.buliangren_1.4.3_143.apk&csr=1bbd",
                new File(folder, "buliangren.apk").getAbsolutePath(), 252_961_666,
                "D5E356B578B18A612A465DD27B6FA8BB"
        );

        DownEntity downEntity3 = new DownEntity("http://imtt.dd.qq.com/16891/EBDD5994F428A8769CD368B91C34FF78.apk?fsname=com.tencent.mm_6.5.10_1080.apk&csr=1bbd",
                new File(folder, "weixin.apk").getAbsolutePath(), 46_258_498,
                "EBDD5994F428A8769CD368B91C34FF78"
        );
        DownEntity downEntity4 = new DownEntity("http://imtt.dd.qq.com/16891/5982E61788AFB7F8C32951BD1784468F.apk?fsname=com.snda.wifilocating_4.2.08_3126.apk&csr=1bbd",
                new File(folder, "wannengyaoshi.apk").getAbsolutePath(), 10_424_648,
                "5982E61788AFB7F8C32951BD1784468F"
        );

        m_downEntities.add(downEntity1);
        m_downEntities.add(downEntity2);
        m_downEntities.add(downEntity3);
        m_downEntities.add(downEntity4);

    }

    Runnable auto_down_runnable = new Runnable() {
        @Override
        public void run() {
            if (!m_dlManager.is_downloading()) {
                Trace.d(TAG, "run() 自动开始下载");
                m_dlManager.add(m_downEntities);
                m_dlManager.execAsync(listener);
            }
            m_handler.postDelayed(this, 5000);
        }
    };
    Handler m_handler = new Handler();

    public void click_start_download(View view) {
        m_dlManager.add(m_downEntities);
        m_dlManager.execAsync(listener);
    }

    public void click_cancel_download(View view) {
        m_dlManager.cancel_all();
    }

    class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return m_downEntities.size();
        }

        @Override
        public Object getItem(int position) {
            return m_downEntities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
//                convertView = LayoutInflater.from(RaiseActivity.this).inflate(R.layout.item_download, parent, false);
                viewHolder = new ViewHolder();
//                viewHolder.msg = (TextView) convertView.findViewById(R.id.textview);
                viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            DownEntity item = (DownEntity) getItem(position);


            return null;
        }

        class ViewHolder {
            TextView msg;
            ProgressBar progressBar;
        }

    }

    IOnDownListener listener = new IOnDownListener() {
        @Override
        public void on_start() {
            Trace.d(TAG, "on_start() ");
        }

        @Override
        public void on_finished(List<DownEntity> successDownEntities, List<DownEntity> failedDownEntities) {
            Trace.w(TAG, "on_finished() ");
            Trace.list(TAG, successDownEntities);
            Trace.list(TAG, failedDownEntities);
        }

        @Override
        public void on_success(DownEntity downEntity) {
            Trace.d(TAG, "on_success() count = " + downEntity);
//            File folder = new File(getFilesDir().getParentFile().getAbsolutePath() + "/download");
//            File[] files = folder.listFiles();
//            if (files == null)
//                return;
////            for (File file : files) {
////                Trace.d(TAG, "on_all_success() 删除文件 = " + file.getName());
////                file.delete();
////            }
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    m_textView.setTextColor(getResources().getColor(android.R.color.black));
//                    m_textView.setText("on_all_success = " + downEntities.size());
//                }
//            });
        }

        @Override
        public void on_failed(final DownEntity downEntity) {
            Trace.d(TAG, "on_failed() err = " + downEntity);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_textView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    m_textView.setText("on failed, err = " + downEntity.download_status);
                }
            });
            long[] longs = {1000, 5000, 1000, 5000, 1000, 5000};
            m_handler.removeCallbacks(auto_down_runnable);
            vibrate(longs, false);
        }

        @Override
        public void on_manual_cancel() {
            Trace.d(TAG, "on_manual_cancel() ");
//                DLManager.getInstance().execAsync(this);
        }

        @Override
        public void on_all_progress(int progress, long downing_size, long total_size) {
//            if (pre_progress != progress) {
            Trace.d(TAG, "on_all_progress() progress = " + progress);
            m_progressBar.setProgress(progress);
            pre_progress = progress;
//            }
        }

        @Override
        public void on_progress(DownEntity entity, int progress, long downing_size, long total_size) {
            Trace.d(TAG, "on_progress() entity = %s, progress = %s", entity.file_size, progress);
        }
    };

    int pre_progress = 0;

    public void vibrate(long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

    public void click_auto_start_download(View view) {

        m_handler.postDelayed(auto_down_runnable, 5000);
    }

    public void click_print_trace(View view) {

        Trace.d(TAG, " d");
        Trace.i(TAG, " i");
        Trace.w(TAG, " w");
        Trace.e(TAG, " e = " + null);

        Trace.d(TAG, " %s,%d", "raise", 1);
        Trace.i(TAG, " %s,%d", "raise", 1);
        Trace.w(TAG, " %s,%d", "raise", 1);
        Trace.e(TAG, new NullPointerException("fu*k null pointer exception."));

        Trace.json(TAG, "{\"name\":\"BeJson\",\"url\":\"http://www.bejson.com\",\"page\":88,\"isNonProfit\":true}");
        Trace.array(TAG, new String[]{"value1", "value2"});
        Trace.list(TAG, Arrays.asList("list1", "list2", "list3"));
    }

    public void click_auto_cancel_download(View view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                m_dlManager.cancel_all();
                m_handler.postDelayed(this, (long) (new Random().nextFloat() * 5000 + 1999));
            }
        }, 2000);
    }
}
