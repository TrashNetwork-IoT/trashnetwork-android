package happyyoung.trashnetwork.net.http;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;

import happyyoung.trashnetwork.net.DataCorruptionException;
import happyyoung.trashnetwork.util.GsonUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public class HttpApiJsonRequest extends HttpApiRequest {
    private Object mRequestData;

    public HttpApiJsonRequest(final Context context, String url, int method, final String token, Object requestData,
                              final HttpApiJsonListener listener) {
        super(context, url, method, token, new HttpListener() {
            @Override
            public void onResponse(@NonNull byte[] data) throws DataCorruptionException {
                if(listener != null)
                    listener.onResponse(data);
            }

            @Override
            public boolean onDataCorrupted(Throwable e) {
                return listener != null && listener.onDataCorrupted(e);
            }

            @Override
            public boolean onErrorResponse(int statusCode, @NonNull byte[] data) throws DataCorruptionException {
                if(listener != null){
                    if(listener.onErrorResponse(statusCode, data))
                        return true;
                    if(statusCode != 500)
                        showError(context, listener.getParsedData().getMessage());
                }
                return false;
            }

            @Override
            public boolean onNetworkError(Throwable e) {
                return listener != null && listener.onNetworkError(e);
            }
        });
        mRequestData = requestData;
    }

    @Override
    public String getBodyContentType() {
        return "application/json";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if(mRequestData == null)
            return new byte[0];
        return GsonUtil.getGson().toJson(mRequestData).getBytes();
    }
}
