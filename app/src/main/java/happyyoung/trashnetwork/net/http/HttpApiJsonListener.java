package happyyoung.trashnetwork.net.http;

import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import happyyoung.trashnetwork.net.DataCorruptionException;
import happyyoung.trashnetwork.net.model.result.Result;
import happyyoung.trashnetwork.util.GsonUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public abstract class HttpApiJsonListener<T extends Result> implements HttpListener {
    private final Class<T> resultType;
    private Result parsedData;

    public Result getParsedData() {
        return parsedData;
    }

    public HttpApiJsonListener(Class<T> resultType){
        this.resultType = resultType;
    }

    protected abstract void onResponse(T data);

    @Override
    public final void onResponse(@NonNull byte[] data) throws DataCorruptionException {
        try {
            parsedData = GsonUtil.getGson().fromJson(new String(data), resultType);
            onResponse((T)parsedData);
        }catch (JsonSyntaxException jse){
            throw new DataCorruptionException(jse.getMessage(), jse);
        }
    }

    protected abstract boolean onErrorResponse(int statusCode, Result errorInfo);

    @Override
    public final boolean onErrorResponse(int statusCode, @NonNull byte[] data) throws DataCorruptionException {
        try {
            parsedData = GsonUtil.getGson().fromJson(new String(data), Result.class);
            return onErrorResponse(statusCode, parsedData);
        }catch (JsonSyntaxException jse){
            throw new DataCorruptionException(jse.getMessage(), jse);
        }
    }
}
