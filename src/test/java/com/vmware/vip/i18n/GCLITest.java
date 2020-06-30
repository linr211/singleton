/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vip.i18n;

import com.vmware.vipclient.i18n.I18nFactory;
import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.base.DataSourceEnum;
import com.vmware.vipclient.i18n.base.cache.FormattingCache;
import com.vmware.vipclient.i18n.base.cache.MessageCache;
import com.vmware.vipclient.i18n.base.instances.TranslationMessage;
import com.vmware.vipclient.i18n.exceptions.VIPClientInitException;
import com.vmware.vipclient.i18n.messages.dto.MessagesDTO;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

public class GCLITest extends BaseTestClass {
    TranslationMessage translation;
    MessagesDTO        dto;

    @Before
    public void init() {
        VIPCfg gc = VIPCfg.getInstance();
        try {
            gc.initialize("vipconfig");
        } catch (VIPClientInitException e) {
            logger.error(e.getMessage());
        }
        gc.initializeVIPService();
        gc.setMessageOrigin(DataSourceEnum.CLI);
        if (gc.getCacheManager() != null)
            gc.getCacheManager().clearCache();
        gc.createTranslationCache(MessageCache.class);
        gc.createFormattingCache(FormattingCache.class);
        I18nFactory i18n = I18nFactory.getInstance(gc);
        translation = (TranslationMessage) i18n.getMessageInstance(TranslationMessage.class);
        dto = new MessagesDTO();
    }



    @Test
    public void testGetMessages() {
        long a = System.currentTimeMillis();
        Map<String, String> m = translation.getStrings(Locale.forLanguageTag("zh-Hans"), "default");
        System.out.println(System.currentTimeMillis()-a);
    }

}
