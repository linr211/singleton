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

    public static JSONObject getMessages(String productName,
                                         String version, String locale, String component) {
        JSONObject o = null;

        VIPCfg gc = VIPCfg.getInstance();
        String gcli = gc.getGcli();
        String gcliBundle = gc.getGcliBundle();
        try {
            Process process = new ProcessBuilder(gcli, "readC", productName, version, locale, component, gcliBundle).start();
            o = handle(process);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }


    private static JSONObject handle(Process process) {
        JSONObject o = null;

        VIPCfg gc = VIPCfg.getInstance();
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String r = builder.toString();
            o = (JSONObject) new JSONParser().parse(r);
            System.out.println(r);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }


    public static JSONObject getPatterns(String locale) {
        JSONObject o = null;

        VIPCfg gc = VIPCfg.getInstance();
        String gcli = gc.getGcli();
        String gcliBundle = gc.getGcliBundle();
        try {
            Process process = new ProcessBuilder(gcli, "readP", gcliBundle, locale).start();
            o = handle(process);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }
}
