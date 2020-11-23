package com.qiangzengy.mall.miaosha.util;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */
public class IpUtil {
    private static final String LOOPBACK_IP = "127.0.0.1";

    public IpUtil() {
    }

    public static String getRealIp() throws SocketException {
        String localIp = null;
        String netip = null;
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        boolean finded = false;

        while(netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = (NetworkInterface)netInterfaces.nextElement();
            Enumeration address = ni.getInetAddresses();

            while(address.hasMoreElements()) {
                ip = (InetAddress)address.nextElement();
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && !ip.getHostAddress().equals("127.0.0.1") && ip.getHostAddress().indexOf(":") == -1) {
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                }

                if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && !ip.getHostAddress().equals("127.0.0.1") && ip.getHostAddress().indexOf(":") == -1) {
                    localIp = ip.getHostAddress();
                }
            }
        }

        return !StringUtils.isEmpty(netip) ? netip : localIp;
    }
}
