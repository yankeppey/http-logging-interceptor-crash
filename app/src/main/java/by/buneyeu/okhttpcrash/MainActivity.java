package by.buneyeu.okhttpcrash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static okhttp3.ws.WebSocket.TEXT;

public class MainActivity extends AppCompatActivity {

    private WebSocketCall mWebSocketCall;
    private String TAG = MainActivity.class.getSimpleName();

    public Subscription mPingSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HttpLoggingInterceptor httpInterceptor = new HttpLoggingInterceptor();
        httpInterceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(httpInterceptor)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(0, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        final Request request = new Request.Builder()
                .url("ws://echo.websocket.org/")
                .build();
        mWebSocketCall = WebSocketCall.create(httpClient, request);
        mWebSocketCall.enqueue(new WebSocketListener() {

            public static final long PING_PERIOD_SECONDS = 30;

            @Override
            public void onMessage(ResponseBody responseBody) throws IOException {
                if (responseBody.contentType() == TEXT) {
                    String str = responseBody.string();
                    Log.d(TAG, "Got text message! " + str);
                } else {
                    Log.d(TAG, "Got binary message! " + responseBody.source().readByteString().hex());
                }
                responseBody.close();
            }

            @Override
            public void onOpen(final WebSocket webSocket, Response response) {
                Log.d(TAG, "Connected!");
                mPingSubscription = Observable.interval(PING_PERIOD_SECONDS, TimeUnit.SECONDS)
                        .subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long time) {
                                try {
                                    Log.d(TAG, "Going to send ping! Timestamp " + time);
                                    webSocket.sendPing(null);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });
            }

            @Override
            public void onFailure(IOException e, Response response) {
                Log.e(TAG, "onFailure!");
            }


            @Override
            public void onPong(Buffer payload) {
                Log.d(TAG, "onPong!");
            }

            @Override
            public void onClose(int code, String reason) {
                Log.d(TAG, "onClose!");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebSocketCall.cancel();
    }
}
