package by.buneyeu.okhttpcrash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebSocketManager mWebSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebSocketManager = new WebSocketManager();
        mWebSocketManager.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebSocketManager.disconnect();
    }
}
