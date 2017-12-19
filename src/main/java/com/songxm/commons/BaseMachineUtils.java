package com.songxm.commons;

import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseMachineUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseMachineUtils.class);

    public BaseMachineUtils() {
    }

    public static String getHostName() {
        String hostName = null;

        try {
            InetAddress address = InetAddress.getLocalHost();
            hostName = address.getHostName();
        } catch (Throwable var3) {
            ;
        }

        if(StringUtils.isNotBlank(hostName) && !"localhost".equalsIgnoreCase(hostName)) {
            return hostName;
        } else {
            try {
                hostName = BaseProcessUtils.run(new String[]{"hostname"});
            } catch (Exception var2) {
                ;
            }

            return StringUtils.isNotBlank(hostName) && !"localhost".equalsIgnoreCase(hostName)?hostName:null;
        }
    }
}
