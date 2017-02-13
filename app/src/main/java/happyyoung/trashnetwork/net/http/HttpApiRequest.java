package happyyoung.trashnetwork.net.http;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.net.DataCorruptionException;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public abstract class HttpApiRequest extends Request {
    private static final String TAG = "HTTP API";
    private Context mContext;
    private HttpListener mListener;
    private Map<String, String> mHeaderMap = new HashMap<>();

    public HttpApiRequest(final Context context, String url, int method, String token, final HttpListener listener) {
        super(method, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null){
                    Log.e(TAG, "Network error", error.getCause());
                    if(listener == null || !listener.onNetworkError(error.getCause())){
                        showError(context, context.getString(R.string.network_error));
                    }
                }else{
                    Log.e(TAG, "Error response", error.getCause());
                    try {
                        byte[] data = error.networkResponse.data;
                        if(data == null)
                            data = new byte[0];
                        if(listener != null){
                            if(listener.onErrorResponse(error.networkResponse.statusCode, data))
                                return;
                        }
                    }catch (DataCorruptionException dce){
                        processCorruptData(context, listener, error.getCause());
                    }
                    if(error.networkResponse.statusCode == 500)
                        showError(context, context.getString(R.string.server_internal_error));
                }
            }
        });
        mContext = context;
        mListener = listener;
        if(token != null && !token.isEmpty())
            mHeaderMap.put("Auth-Token", token);
    }

    Context getContext() {
        return mContext;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaderMap;
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        byte[] data = response.data;
        if(data == null)
            data = new byte[0];
        Log.i(TAG, "Received " + data.length + "bytes data, HTTP status code:" + response.statusCode);
        return Response.success(data, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Object response) {
        try {
            if (mListener != null)
                mListener.onResponse((byte[])response);
        } catch (DataCorruptionException e) {
            processCorruptData(mContext, mListener, e);
        }
    }

    private static void processCorruptData(Context mContext, HttpListener mListener, Throwable e){
        Log.e(TAG, "Received corrupted data", e);
        if(mListener == null || !mListener.onDataCorrupted(e)){
            showError(mContext, mContext.getString(R.string.response_data_corrupt));
        }
    }

    static void showError(Context mContext, String error){
        if(mContext instanceof Activity)
            Snackbar.make(((Activity) mContext).findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
    }
}
