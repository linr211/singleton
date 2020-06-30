/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.messages.api.opt.local;

import com.vmware.vipclient.i18n.messages.api.opt.Opt;
import com.vmware.vipclient.i18n.messages.dto.MessagesDTO;
import com.vmware.vipclient.i18n.util.CLIBundleUtil;
import com.vmware.vipclient.i18n.util.JSONBundleUtil;
import org.json.simple.JSONObject;

public class CLIMessagesOpt implements Opt {
    private MessagesDTO dto;

    public CLIMessagesOpt(MessagesDTO dto) {
        this.dto = dto;
    }

    public JSONObject getComponentMessages() {
        return CLIBundleUtil.getMessages(dto.getProductID(),
                dto.getVersion(), dto.getLocale(), dto.getComponent());
    }

    public String getString() {
        JSONObject jo = this.getComponentMessages();
        String k = dto.getKey();
        String v = "";
        if (jo != null) {
            v = jo.get(k) == null ? "" : v;
        }
        return v;
    }
}
