/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.messages.api.opt.local;

import com.vmware.vipclient.i18n.l2.common.PatternKeys;
import com.vmware.vipclient.i18n.util.CLIBundleUtil;
import com.vmware.vipclient.i18n.util.PatternBundleUtil;
import org.json.simple.JSONObject;

import java.util.Map;

public class CLIPatternOpt {

    public JSONObject getPatternsByLocale(String locale) {
        Map<String, Object> patterns = CLIBundleUtil.getPatterns(locale);
        if (patterns == null) {
            return null;
        } else {
            return (JSONObject) patterns.get(PatternKeys.CATEGORIES);
        }
    }
}
