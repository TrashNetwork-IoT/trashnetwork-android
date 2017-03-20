package happyyoung.trashnetwork.cleaning.net.http;

import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import happyyoung.trashnetwork.cleaning.net.DataCorruptionException;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.util.GsonUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public abstract class HttpApiJsonListener<T extends Result> implements HttpListener {
    private final Class<T> resultType;
    private Result parsedData;

    Result getParsedData() {
        return parsedData;
    }

    public HttpApiJsonListener(Class<T> resultType){
        this.resultType = resultType;
    }

    public abstract void onResponse(T data);

    @Override
    public final void onResponse(@NonNull byte[] data) throws DataCorruptionException {
        try {
            parsedData = GsonUtil.getGson().fromJson(new String(data), resultType);
            onResponse((T)parsedData);
        }catch (JsonSyntaxException jse){
            throw new DataCorruptionException(jse.getMessage(), jse);
        }
    }

    public abstract boolean onErrorResponse(int statusCode, Result errorInfo);

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
