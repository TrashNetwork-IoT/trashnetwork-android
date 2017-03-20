package happyyoung.trashnetwork.cleaning.net.http;

import android.support.annotation.NonNull;
import happyyoung.trashnetwork.cleaning.net.DataCorruptionException;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public interface HttpListener {
    void onResponse(@NonNull byte[] data) throws DataCorruptionException;
    boolean onDataCorrupted(Throwable e);
    boolean onErrorResponse(int statusCode, @NonNull byte[] data) throws DataCorruptionException;
    boolean onNetworkError(Throwable e);    //No network or connect timeout
}
