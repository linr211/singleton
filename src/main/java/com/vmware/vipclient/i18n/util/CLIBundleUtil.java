/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.util;

import com.vmware.vipclient.i18n.VIPCfg;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CLIBundleUtil {
    final static String COMMAND = "readC";

    public static JSONObject getMessages(String productName,
                                         String version, String locale, String component) {
        JSONObject o = null;

        VIPCfg gc = VIPCfg.getInstance();
        String gcli = gc.getGcli();
        String gcliBundle = gc.getGcliBundle();
        try {
            Process process = new ProcessBuilder(gcli, COMMAND, productName, version, locale, component, gcliBundle).start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String r = builder.toString();
            o = (JSONObject)new JSONParser().parse(r);
            System.out.println(r);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }
}
