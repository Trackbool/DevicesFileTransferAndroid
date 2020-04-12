package com.afa.devicesfiletransfer.services.discovery;

import com.afa.devicesfiletransfer.domain.model.DeviceFactory;
import com.afa.devicesfiletransfer.domain.model.DiscoveryOperation;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Set;

public class DiscoveryProtocolSender {
    private NetworkDataProvider network;
    private int port;
    private DatagramSocket socket;

    public DiscoveryProtocolSender(NetworkDataProvider network, int port) {
        this.network = network;
        this.port = port;
    }

    public void discover() throws SocketException {
        socket = new DatagramSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                broadcastDiscovery();
                socket.close();
            }
        }).start();
    }

    private void broadcastDiscovery() {
        Set<InetAddress> addresses = network.getIpv4BroadcastAddresses();
        for (InetAddress a : addresses) {
            try {
                sendDiscovery(a);
            } catch (IOException ignored) {
            }
        }
    }

    private void sendDiscovery(InetAddress address) throws IOException {
        DiscoveryOperation discoveryOperation =
                new DiscoveryOperation("discovery", DeviceFactory.getCurrentDeviceProperties());
        byte[] sendData = new Gson().toJson(discoveryOperation).getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);
    }
}
