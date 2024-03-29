package com.uoscs09.theuos2.base;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.concurrent.Callable;

/**
 * 비 동기 작업이 필요한 AppWidget 을 위한 Abstract Class
 */
public abstract class AbsAsyncWidgetProvider<Data> extends BaseAppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {

        final PendingResult pendingResult = goAsync();
        AsyncUtil.newRequest(new Callable<Data>() {
            @Override
            public Data call() throws Exception {
                return doInBackGround(context, appWidgetManager, appWidgetIds);
            }
        }).getAsync(
                new Request.ResultListener<Data>() {
                    @Override
                    public void onResult(Data result) {
                        AbsAsyncWidgetProvider.this.onBackgroundTaskResult(context, appWidgetManager, appWidgetIds, result);
                        pendingResult.finish();
                    }
                },
                new Request.ErrorListener() {
                    @Override
                    public void onError(Exception e) {
                        AbsAsyncWidgetProvider.this.exceptionOccurred(context, appWidgetManager, appWidgetIds, e);
                        pendingResult.finish();
                    }
                }
        );

    }

    /**
     * 다른 Thread 에서 작업을 할 때 호출된다.
     *
     * @return 작업한 결과
     */
    protected abstract Data doInBackGround(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) throws Exception;

    /**
     * 다른 Thread 에서의 작업이 성공적으로 끝나고, 메인 Thread 에서 호출된다.
     *
     * @param result - {@link #doInBackGround(Context, AppWidgetManager, int[])}에서
     *               반환한 결과
     */
    protected abstract void onBackgroundTaskResult(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Data result);

    /**
     * 다른 Thread 에서의 작업 도중, 처리되지 않은 Exception 이 발생하였을 때 호출된다.
     *
     * @param e 발생한 Exception
     */
    protected void exceptionOccurred(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Exception e) {
        AppUtil.showErrorToast(context, e, true);
    }
}
