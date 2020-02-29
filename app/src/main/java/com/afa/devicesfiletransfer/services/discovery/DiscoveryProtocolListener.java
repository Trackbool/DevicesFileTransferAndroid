package com.afa.devicesfiletransfer.services.discovery;

import com.afa.devicesfiletransfer.model.DeviceProperties;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscoveryProtocolListener {
    private final NetworkDataProvider networkDataProvider;
    private final String devicePropertiesJson;
    private final int port;
    private Callback callback;
    private DatagramSocket serverSocket;
    private AtomicBoolean listening;

    public DiscoveryProtocolListener(NetworkDataProvider networkDataProvider,
                                     DeviceProperties deviceProperties,
                                     int port) {
        this.networkDataProvider = networkDataProvider;
        this.devicePropertiesJson = new Gson().toJson(deviceProperties);
        this.port = port;
        this.listening = new AtomicBoolean(false);
    }

    public DiscoveryProtocolListener(NetworkDataProvider networkDataProvider,
                                     DeviceProperties deviceProperties,
                                     int port, Callback callback) {
        this(networkDataProvider, deviceProperties, port);
        this.callback = callback;
    }

    public void start() throws SocketException {
        if (listening.get()) {
            throw new IllegalStateException("Listener already listening");
        }

        serverSocket = new DatagramSocket(port);
        listening.set(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                listen();
            }
        }).start();
    }

    public void stop() {
        listening.set(false);
        serverSocket.close();
    }

    private void listen() {
        while (listening.get()) {
            try {
                DatagramPacket receivePacket = receiveRequest();
                InetAddress senderAddress = receivePacket.getAddress();
                int senderPort = receivePacket.getPort();
                String receivedMessage = new String(receivePacket.getData(),
                        receivePacket.getOffset(),
                        receivePacket.getLength());

                if (receivedIpIsCurrentDeviceIp(senderAddress)) {
                    continue;
                }

                if (isDiscoveryMessage(receivedMessage)) {
                    if (callback != null) {
                        callback.discoveryRequestReceived(senderAddress, senderPort);
                    }
                    sendResponse(senderAddress);
                } else {
                    if (callback != null) {
                        notifyDiscoveryResponse(senderAddress, senderPort, receivedMessage);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private DatagramPacket receiveRequest() throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        return receivePacket;
    }

    private void sendResponse(InetAddress senderAddress) throws IOException {
        byte[] sendData = devicePropertiesJson.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, senderAddress, port);
        serverSocket.send(sendPacket);
    }

    private boolean receivedIpIsCurrentDeviceIp(InetAddress receivedAddress) {
        Set<InetAddress> currentDeviceAddresses = networkDataProvider.getDeviceIpv4Addresses();
        String receivedIp = receivedAddress.getHostAddress();
        for (InetAddress a : currentDeviceAddresses) {
            String currentDeviceIp = a.getHostAddress();
            if (receivedIp.equals(currentDeviceIp)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDiscoveryMessage(String message) {
        return message.equals("discovery");
    }

    private void notifyDiscoveryResponse(InetAddress senderAddress, int senderPort, String message) {
        try {
            DeviceProperties deviceProperties = new Gson()
                    .fromJson(message, DeviceProperties.class);
            callback.discoveryResponseReceived(senderAddress, senderPort, deviceProperties);
        } catch (Exception e) {
            System.err.println("Protocol message error: " + e.getMessage());
        }
    }

    public interface Callback {
        void discoveryRequestReceived(InetAddress senderAddress, int senderPort);

        void discoveryResponseReceived(InetAddress senderAddress, int senderPort, DeviceProperties deviceProperties);
    }
}
