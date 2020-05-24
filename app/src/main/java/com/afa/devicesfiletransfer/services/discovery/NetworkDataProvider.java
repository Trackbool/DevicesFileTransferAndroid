package com.afa.devicesfiletransfer.services.discovery;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class NetworkDataProvider {

    public boolean isCurrentDeviceIp(InetAddress receivedAddress) {
        Set<InetAddress> currentDeviceAddresses = getDeviceIpv4Addresses();
        String receivedIp = receivedAddress.getHostAddress();
        for (InetAddress a : currentDeviceAddresses) {
            String currentDeviceIp = a.getHostAddress();
            if (receivedIp.equals(currentDeviceIp)) {
                return true;
            }
        }
        return false;
    }

    public Set<InetAddress> getDeviceIpv4Addresses() {
        Set<InetAddress> addresses = new HashSet<>();

        try {
            addresses = getDeviceIpv4AddressesInternal();
        } catch (SocketException ignored) {
        }

        return addresses;
    }

    public Set<InetAddress> getIpv4BroadcastAddresses() {
        Set<InetAddress> addresses = new HashSet<>();

        try {
            addresses = getIpv4BroadcastAddressesInternal();
        } catch (SocketException ignored) {
        }

        return addresses;
    }

    private Set<InetAddress> getDeviceIpv4AddressesInternal() throws SocketException {
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

                if (address instanceof Inet6Address)
                    continue;

                addresses.add(address);
            }
        }

        return addresses;
    }

    private Set<InetAddress> getIpv4BroadcastAddressesInternal() throws SocketException {
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
                if (broadcast == null || broadcast instanceof Inet6Address)
                    continue;

                addresses.add(broadcast);
            }
        }

        return addresses;
    }
}
