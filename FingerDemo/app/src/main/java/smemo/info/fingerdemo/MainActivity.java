package smemo.info.fingerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements FingerprintController.FingerAuthListener {

    private int code = FingerprintController.FINGER_SUCCESS;

    private TextView toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toast = (TextView) findViewById(R.id.toast);

        code = FingerprintController.getInstance(this).checkFingerEnable();
        if (code == FingerprintController.FINGER_SUCCESS) {
            FingerprintController.getInstance(this).setAuthListener(this);
            setToast("可以启动指纹识别");
        } else {
            switch (code) {
                case FingerprintController.FINGER_ERROR_NO_HARDWARE:
                    setToast("该设备不支持指纹识别");
                    break;
                case FingerprintController.FINGER_ERROR_NO_PERMISSION:
                    setToast("当前应用没有指纹识别权限");
                    break;
                case FingerprintController.FINGER_ERROR_NO_FINGER:
                    setToast("当前设备没有录入指纹,请前往录入指纹");
                    break;
            }
        }

    }

    public void start(View view) {
        if (code == FingerprintController.FINGER_SUCCESS) {
            FingerprintController.getInstance(this).startFingerAuth();
            setToast("开始指纹识别");
        }
    }

    public void cancel(View view) {
        if (code == FingerprintController.FINGER_SUCCESS) {
            FingerprintController.getInstance(this).cancelFingerAuth();
        }
    }


    @Override
    public void success() {
        setToast("识别成功");
    }

    @Override
    public void error(String error) {
        setToast(error);
    }

    @Override
    public void help(String msg) {
        setToast(msg);
    }

    @Override
    public void cancel() {
        setToast("取消指纹识别");
    }

    @Override
    public void failure() {
        setToast("指纹识别失败");
    }

    public void setToast(String msg) {
        toast.setText("提示:" + msg);
    }
}
