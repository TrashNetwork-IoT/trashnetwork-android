package happyyoung.trashnetwork.cleaning.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;

import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.request.PostWorkRecordRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-24
 */
public class HttpUtil {
    public static void postWorkRecord(final Context context, long trashId){
        final AlertDialog errorDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setPositiveButton(R.string.action_ok, null)
                .setCancelable(false)
                .create();
        if(GlobalInfo.currentLocation == null ||
                System.currentTimeMillis() - GlobalInfo.currentLocation.getUpdateTime().getTime() > 30 * 1000){
            errorDialog.setMessage(context.getString(R.string.alert_location_outdate));
            errorDialog.show();
            return;
        }
        PostWorkRecordRequest request = new PostWorkRecordRequest(trashId,
                GlobalInfo.currentLocation.getLongitude(), GlobalInfo.currentLocation.getLatitude());
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.alert_posting_work_record));
        pd.setCancelable(false);
        pd.show();
        HttpApi.startRequest(new HttpApiJsonRequest(context, HttpApi.getApiUrl(HttpApi.WorkRecordApi.POST_RECORD), Request.Method.POST, GlobalInfo.token, request,
                new HttpApiJsonListener<Result>(Result.class) {
                    @Override
                    public void onResponse(Result data) {
                        pd.dismiss();
                        Toast.makeText(context, data.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public boolean onErrorResponse(int statusCode, Result errorInfo) {
                        pd.dismiss();
                        errorDialog.setMessage(errorInfo.getMessage());
                        errorDialog.show();
                        return true;
                    }

                    @Override
                    public boolean onDataCorrupted(Throwable e) {
                        pd.dismiss();
                        return false;
                    }

                    @Override
                    public boolean onNetworkError(Throwable e) {
                        pd.dismiss();
                        return false;
                    }
                }));
    }
}
