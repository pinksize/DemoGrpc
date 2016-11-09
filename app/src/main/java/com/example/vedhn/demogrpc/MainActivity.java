package com.example.vedhn.demogrpc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.vedhn.demofcm.protobuf.DeviceGrpc;
import com.example.vedhn.demofcm.protobuf.MessageGrpc;
import com.example.vedhn.demofcm.protobuf.Vedfcm;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MainActivity extends AppCompatActivity {

    private final String HOST = "104.199.151.55";
    private final int PORT = 8080;
    private ManagedChannel mChannel;
    private DeviceGrpc.DeviceBlockingStub deviceBlockingStub;
    private DeviceGrpc.DeviceStub deviceAsyncStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChannel = ManagedChannelBuilder.forAddress(HOST, PORT).usePlaintext(true).build();

        deviceBlockingStub = DeviceGrpc.newBlockingStub(mChannel);
        deviceAsyncStub = DeviceGrpc.newStub(mChannel);

        Vedfcm.DeviceInfo deviceInfo = Vedfcm.DeviceInfo.newBuilder().setIidToken("some id").build();

        Vedfcm.DeviceInfo registeredDevice = deviceBlockingStub.register(deviceInfo);

    }

}
