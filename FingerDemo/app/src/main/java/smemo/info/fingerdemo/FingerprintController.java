package smemo.info.fingerdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

public class FingerprintController {

    private static FingerprintController sSingleton = null;

    private Context mContext;

    private FingerprintManagerCompat manager;

    private static final String TAG = "FingerprintController";
    private static final String PREMISSION = "android.permission.USE_FINGERPRINT";

    //成功
    public static final int FINGER_SUCCESS = 0;
    //硬件不支持
    public static final int FINGER_ERROR_NO_HARDWARE = 1;
    //没有申请权限
    public static final int FINGER_ERROR_NO_PERMISSION = 2;
    //用户没有赋予权限
    //Protection level: normal
    //指纹权限的级别是normal,理论上不需要动态权限认证
    public static final int FINGER_ERROR_NO_USER_PERMISSION = 3;
    //用户没有储存指纹
    public static final int FINGER_ERROR_NO_FINGER = 4;

    //取消指纹识别
    private CancellationSignal cancellationSignal;

    private FingerAuthListener mAuthListener;

    public static synchronized FingerprintController getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new FingerprintController(context);
        }
        return sSingleton;
    }

    public FingerprintController(Context context) {
        mContext = context;
        manager = FingerprintManagerCompat.from(this.mContext);
    }

    /**
     * 开始指纹识别
     * 失败次数过多后需要一定时间后才可以重新启动
     */
    public void startFingerAuth() {
        if (null == cancellationSignal) {
            cancellationSignal = new CancellationSignal();
        }
        manager.authenticate(null, 0, cancellationSignal, new FingerAuthCallBack(), null);
    }

    /**
     * 取消指纹识别
     */
    public void cancelFingerAuth() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            if (mAuthListener != null)
                mAuthListener.cancel();
        }
    }

    /**
     * 指纹识别回调
     */
    public class FingerAuthCallBack extends FingerprintManagerCompat.AuthenticationCallback {

        // 当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息
        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
//            Log.d(TAG, "onAuthenticationError: " + errString);
            if (null != mAuthListener)
                mAuthListener.error(errString.toString());
        }

        // 当指纹验证失败的时候会回调此函数，失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
        @Override
        public void onAuthenticationFailed() {
            if (null != mAuthListener)
                mAuthListener.failure();
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            if (null != mAuthListener)
                mAuthListener.help(helpString.toString());
        }

        // 当验证的指纹成功时会回调此函数，然后不再监听指纹sensor
        @Override
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
            if (null != mAuthListener)
                mAuthListener.success();
        }

    }

    /**
     * 检查指纹解锁是否可用
     *
     * @return 状态
     */
    public int checkFingerEnable() {
        if (null == manager) {
            manager = FingerprintManagerCompat.from(mContext);
        }
        if (!isAppPermissionEnable()) {
            return FINGER_ERROR_NO_PERMISSION;
        }
        if (!manager.isHardwareDetected()) {
            return FINGER_ERROR_NO_HARDWARE;
        }
        if (!manager.hasEnrolledFingerprints()) {
            return FINGER_ERROR_NO_FINGER;
        }
        if (!isUserPermissionEnable()) {
            return FINGER_ERROR_NO_USER_PERMISSION;
        }
        return FINGER_SUCCESS;
    }

    /**
     * 是否声明了该权限
     */
    private boolean isAppPermissionEnable() {
        PackageManager pm = mContext.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "can't get packagemanager");
            return true;
        }
        try {
            return PackageManager.PERMISSION_GRANTED == pm.checkPermission(PREMISSION, mContext.getPackageName());
        } catch (Exception e) {
            Log.w(TAG, "can't checkt Permission " + e.getMessage());
            return true;
        }
    }

    /**
     * 是否具有动态权限,理论上不需要验证
     */
    private boolean isUserPermissionEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED == mContext.checkSelfPermission(Manifest.permission.USE_FINGERPRINT);
        }
        return true;
    }

    public void setAuthListener(FingerAuthListener authListener) {
        mAuthListener = authListener;
    }

    public interface FingerAuthListener {

        void success();

        void error(String error);

        void help(String msg);

        void cancel();

        void failure();

    }

}
