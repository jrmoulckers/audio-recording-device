package com.utaustin.ard.network;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.utaustin.ard.constants.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// TODO: REMOVE ME!!! Based on https://github.com/shoaibsaikat/Android-Channel-Client-Stream/blob/99a0bd9ac1516449f3258e36031d0bfcbf122958/wear/src/main/java/com/example/myapplication/MainActivity.java#L149
public class OfflineTransferActivity extends WearableActivity {
    private static final String DEBUG = Constants.DEBUG_OTA;
    private static final String AUDIO_CHANNEL_ID = "com.utaustin.ard.network.audiotransmissionchannel";


    private ExecutorService executorService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executorService = new ThreadPoolExecutor(4, 5, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
    }

    @Override
    public void onResume() {
        super.onResume();

        Wearable.getChannelClient(getApplicationContext()).registerChannelCallback(new ChannelClient.ChannelCallback() {
            @Override
            public void onChannelOpened(@NonNull final ChannelClient.Channel channel) {
                super.onChannelOpened(channel);
                Log.d(DEBUG, "Channel opened... Reading.");
                Task<InputStream> inputStreamTask = Wearable.getChannelClient(getApplicationContext()).getInputStream(channel);
                inputStreamTask.addOnSuccessListener(new OnSuccessListener<InputStream>() {
                    @Override
                    public void onSuccess(final InputStream inputStream) {
                        Log.d(DEBUG, "Successfully retrieved input stream.");
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String text = "";
                                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                    int read;
                                    byte[] data = new byte[1024];

                                    while ((read = inputStream.read(data, 0, data.length)) != -1) {
                                        Log.d(DEBUG, "Data length: " + read);
                                        buffer.write(data, 0, read);

                                        buffer.flush();
                                        byte[] byteArray = buffer.toByteArray();

                                        text += new String(byteArray, StandardCharsets.UTF_8);
                                    }

                                    Log.d(DEBUG, "Reading data: " + text);
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    Wearable.getChannelClient(getApplicationContext()).close(channel);
                                    Log.d(DEBUG, "Closed channel.");
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    // Destroy async executor service prior to destroying activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG, "Destroying executor service.");
        executorService.shutdown();
    }

    // Return all nodes connected to this wearable node
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
        Log.d(DEBUG, "Retrieving connected nodes.");
        try {
            List<Node> nodes = Tasks.await(nodeListTask);
            for (Node node : nodes) {
                results.add(node.getId());
                Log.d(DEBUG, "Found node: " + node.getId());
            }
        } catch (ExecutionException exception) {
            Log.e(DEBUG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(DEBUG, "Interrupt occurred: " + exception);
        }

        Log.d(DEBUG, "Successfully retrieved " + results.size() + " nodes.");
        return results;
    }

//    public void onSend(View view) {
//        final String text = etMessage.getText().toString();
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                Collection<String> nodes = getNodes();
//                Log.e(DEBUG, "Nodes: " + nodes.size());
//                for (String node : nodes) {
//                    Task<ChannelClient.Channel> channelTask = Wearable.getChannelClient(getApplicationContext()).openChannel(node, AUDIO_CHANNEL_ID);
//                    channelTask.addOnSuccessListener(new OnSuccessListener<ChannelClient.Channel>() {
//                        @Override
//                        public void onSuccess(ChannelClient.Channel channel) {
//                            Log.e(DEBUG, "Node (ID: " + channel.getNodeId() + ")  onSuccess");
//                            Task<OutputStream> outputStreamTask = Wearable.getChannelClient(getApplicationContext()).getOutputStream(channel);
//                            outputStreamTask.addOnSuccessListener(new OnSuccessListener<OutputStream>() {
//                                @Override
//                                public void onSuccess(OutputStream outputStream) {
//                                    Log.d(DEBUG, "Output stream task onSuccess");
//                                    try {
//                                        outputStream.write(text.getBytes());
//                                        outputStream.flush();
//                                        outputStream.close();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//                        }
//                    });
//                }
//            }
//        });
//    }
}
