package com.example.vedhn.demogrpc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.vedhn.demofcm.protobuf.DeviceGrpc;
import com.example.vedhn.demofcm.protobuf.MessageGrpc;
import com.example.vedhn.demofcm.protobuf.Vedfcm;
import com.google.firebase.iid.FirebaseInstanceId;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = getClass().getSimpleName();

    private final String HOST = "104.199.151.55";
    private final int PORT = 8080;

    private ManagedChannel mChannel;
    private DeviceGrpc.DeviceStub deviceAsyncStub;
    private Vedfcm.StreamSubscription subscription;
    private BroadcastReceiver receiver;
    private Button subscribe;
    private Button unsubscribe;
    private TextView result1;
    private TextView result2;
    private String refreshedToken;
    private Button sendByStream;
    private MessageGrpc.MessageStub messageStub;
    private Vedfcm.SendToStreamMessage streamMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onCreate: refreshToken = " + refreshedToken);

        subscribe = (Button) findViewById(R.id.btnSubscribe);
        unsubscribe = (Button) findViewById(R.id.btnUnsubscibe);
        sendByStream = (Button) findViewById(R.id.btnSendByStream);
        result1 = (TextView) findViewById(R.id.textViewResult1);
        result1.setMovementMethod(new ScrollingMovementMethod());
        result2 = (TextView) findViewById(R.id.textViewResult2);
        result2.setMovementMethod(new ScrollingMovementMethod());

        subscribe.setOnClickListener(this);
        unsubscribe.setOnClickListener(this);
        sendByStream.setOnClickListener(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "token was refresh: refreshToken = " + refreshedToken);
                refreshedToken = intent.getStringExtra("IID");
            }
        };
    }

    private void sendSubscribeMessage(String iid) {
        result1.setText("");
        mChannel = ManagedChannelBuilder.forAddress(HOST, PORT).usePlaintext(true).build();

        deviceAsyncStub = DeviceGrpc.newStub(mChannel);

        subscription = Vedfcm.StreamSubscription.newBuilder().setIidToken(iid).setStreamId("13").build();

        Log.d(TAG, "subscribe stream");
        deviceAsyncStub.subscribeStream(subscription, new StreamObserver<Vedfcm.NoneResponse>() {
            @Override
            public void onNext(final Vedfcm.NoneResponse value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result1.setText(
                                result1.getText() + "\nsubscribe : onNext() called with: value = [" + value + "]");
                    }
                });
            }

            @Override
            public void onError(final Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result1.setText(result1.getText() + "\nsubscribe : onError() called with: t = [" + t + "]");
                    }
                });
            }

            @Override
            public void onCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result1.setText(result1.getText() + "\nsubscribe : onCompleted() called");
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter("com.example.vedhn.demogrpc.IID_REFRESHED"));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (receiver != null)
            unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnUnsubscibe:
            unsubscribeStream();
            break;
        case R.id.btnSubscribe:
            if (!TextUtils.isEmpty(refreshedToken)) {
                sendSubscribeMessage(refreshedToken);
            }
            break;
        case R.id.btnSendByStream:
            if (!TextUtils.isEmpty(refreshedToken)) {
                sendByStream();
            }
            break;
        }
    }

    private void sendByStream() {
        result2.setText("");
        mChannel = ManagedChannelBuilder.forAddress(HOST, PORT).usePlaintext(true).build();

        messageStub = MessageGrpc.newStub(mChannel);

        Vedfcm.MessagePayload.Notification.Builder nBuilder = Vedfcm.MessagePayload.Notification.newBuilder();
        nBuilder.setTitle("sample title").setBody("sample body").setIcon("sample icon").setClickAction("simple action")
                .setSound("sample sound").setTag("sample tag").setBodyLocKey("sample body loc key")
                .setBodyLocArgs("sample body loc args").setTitleLocKey("sample title loc key")
                .setTitleLocArgs("sample title loc args").setBadge("sample badge");
        Vedfcm.MessagePayload msgPayload =
                Vedfcm.MessagePayload.newBuilder().setData("{}").setNotification(nBuilder.build()).build();

        Vedfcm.SendToStreamMessage.Builder msgBuilder = Vedfcm.SendToStreamMessage.newBuilder();
        msgBuilder.setStreamId("13");
        msgBuilder.setPayload(msgPayload);
        streamMessage = msgBuilder.build();

        Log.d(TAG, "send by stream");
        messageStub.sendByStream(streamMessage, new StreamObserver<Vedfcm.NoneResponse>() {
            @Override
            public void onNext(final Vedfcm.NoneResponse value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result2.setText(result2.getText() + "\nsendByStream : onNext() called with: value = [" +
                                                value + "]");
                    }
                });
            }

            @Override
            public void onError(final Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result2.setText(result2.getText() + "\nsendByStream : onError() called with: t = [" + t + "]");
                    }
                });
            }

            @Override
            public void onCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result2.setText(result2.getText() + "\nsendByStream : onCompleted() called");
                    }
                });
            }
        });
    }

    private void unsubscribeStream() {
        result1.setText("");
        Log.d(TAG, "unsubscribe stream");
        deviceAsyncStub.unsubscribeStream(subscription, new StreamObserver<Vedfcm.NoneResponse>() {
            @Override
            public void onNext(final Vedfcm.NoneResponse value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result1.setText(
                                result1.getText() + "\nunsubscribe : onNext() called with: value = [" + value + "]");
                    }
                });
            }

            @Override
            public void onError(final Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result1.setText(result1.getText() + "\nunsubscribe : onError() called with: t = [" + t + "]");
                    }
                });
            }

            @Override
            public void onCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result1.setText(result1.getText() + "\nunsubscribe : onCompleted() called");
                    }
                });
            }
        });
    }
}
