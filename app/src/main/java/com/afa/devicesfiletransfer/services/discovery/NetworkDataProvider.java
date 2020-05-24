package com.afa.devicesfiletransfer.services.discovery;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class NetworkDataProvider {

    public boolean isCurrentDeviceAddress(InetAddress address) {
        Set<InetAddress> currentDeviceAddresses = getDeviceAddresses();
        String receivedIp = address.getHostAddress();
        for (InetAddress a : currentDeviceAddresses) {
            String currentDeviceIp = a.getHostAddress();
            if (receivedIp.equals(currentDeviceIp)) {
                return true;
            }
        }
        return false;
    }

    public InetAddress getOutgoingDeviceIp() throws IOException {
        InetAddress address;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            address = socket.getLocalAddress();
        }
        return address;
    }

    public Set<InetAddress> getDeviceAddresses() {
        Set<InetAddress> addresses = new HashSet<>();

        try {
            addresses = getDeviceAddressesInternal();
        } catch (SocketException ignored) {
        }

        return addresses;
    }

    private Set<InetAddress> getDeviceAddressesInternal() throws SocketException {
        Set<InetAddress> addresses = new HashSet<>();

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            try {
                if (networkInterface.isLoopback() || !networkInterface.isUp())
                    continue;
            } catch (SocketException ignored) {
            }

            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress address = inetAddresses.nextElement();
                addresses.add(address);
            }
        }

        return addresses;
    }

    public Set<InetAddress> getBroadcastAddresses() {
        Set<InetAddress> addresses = new HashSet<>();

        try {
            addresses = getBroadcastAddressesInternal();
        } catch (SocketException ignored) {
        }

        return addresses;
    }

    private Set<InetAddress> getBroadcastAddressesInternal() throws SocketException {
        Set<InetAddress> addresses = new HashSet<>();

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            try {
                if (networkInterface.isLoopback() || !networkInterface.isUp())
                    continue;
            } catch (SocketException ignored) {
            }
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null)
                    continue;

                addresses.add(broadcast);
            }
        }

        return addresses;
    }
}
