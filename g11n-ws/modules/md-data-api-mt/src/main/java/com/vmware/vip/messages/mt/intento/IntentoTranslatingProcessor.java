/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vip.messages.mt.intento;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vmware.vip.common.http.HTTPRequester;
import com.vmware.vip.common.utils.LocaleUtils;
import com.vmware.vip.messages.data.dao.api.IMTProcessor;
import com.vmware.vip.messages.data.dao.exception.MTException;
import com.vmware.vip.messages.mt.MTConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * a processor to handle Inteto MT
 */
public class IntentoTranslatingProcessor implements IMTProcessor {
    private static Logger logger = LoggerFactory.getLogger(IntentoTranslatingProcessor.class);

    /**
     * get a string's MT from Intento
     *
     * @param fromLang
     * @param toLang
     * @param source
     * @return
     * @throws MTException
     */
    public String translateStr(String fromLang, String toLang, String source)
            throws MTException {
        List<String> sourceList = new ArrayList<>();
        sourceList.add(source);
        List<String> translationList = this.translateArray(fromLang, toLang,
                sourceList);
        return translationList.size() == 1 ? translationList.get(0) : "";
    }

    /**
     * get an array of strings from Intento, it contains posting sources to Intento and uses returned {id} to get the sources' translation
     *
     * @param fromLang
     * @param toLang
     * @param sourceList
     * @return
     * @throws MTException
     */
    public List<String> translateArray(String fromLang, String toLang,
                                       List<String> sourceList) throws MTException {
        String key = LocaleUtils.normalizeToLanguageTag(fromLang) + "_" + LocaleUtils.normalizeToLanguageTag(toLang);
        if (MTConfig.isTranslatedFull(key)) {
            throw new MTException("Request failed: daily MT translated wordcount is over!");
        }

        ObjectNode resultNode = null;
        try {
            logger.trace("-----Start to request MT translation----------");
            FromModel fromObj = this.getFromObj(
                    LocaleUtils.normalizeToLanguageTag(fromLang),
                    LocaleUtils.normalizeToLanguageTag(toLang), sourceList);
            ObjectMapper mapper = new ObjectMapper();
            String requestJson = mapper.writeValueAsString(fromObj);
            String urlStr = MTConfig.MTSERVER;
            logger.trace("Using Intento MT Server:");
            logger.trace(MTConfig.MTSERVER);
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", IntentoConfig.API_KEY);
            String response = HTTPRequester.postData(requestJson, urlStr,
                    "application/json", "POST", headers);
            if (response == null || response.equalsIgnoreCase("")) {
                throw new MTException("Request failed: respond empty from MT server!");
            }
            resultNode = mapper.readValue(response, ObjectNode.class);
        } catch (IOException e) {
            logger.error("Failed to request MT translation");
        }

        ToResult resultArrayNode = null;
        if (resultNode != null) {
            resultArrayNode = this.getAsyncTranslations(resultNode.get("id").asText());
        } else {
            throw new MTException("Getting MT async translation is failed!");
        }
        if (resultArrayNode != null && resultArrayNode.getResponse().size() > 0) {
            MTConfig.updateTranslationCache(key, resultArrayNode.getResponse().size());
            return resultArrayNode.getResponse().get(0).getResults();
        } else {
            throw new MTException("MT response is null or size is 0!");
        }
    }

    /**
     * get a FromModel object from an array of sources for MT request
     *
     * @param fromLang
     * @param toLang
     * @param sourceList
     * @return
     */
    private FromModel getFromObj(String fromLang, String toLang,
                                 List<String> sourceList) {
        ContextModel context = new ContextModel();
        context.setFrom(fromLang);
        context.setTo(toLang);
        context.setCategory(IntentoConfig.CATEGORY);
        context.setText(sourceList);
        ServiceModel service = new ServiceModel();
        service.setAsync(true);
        List<String> providers = new ArrayList<>();
        providers.add(IntentoConfig.PROVIDER);
        Map<String, List<Map<String, String>>> auth = new HashMap<>();
        service.setProvider(providers);
        List<Map<String, String>> klist = new ArrayList<>();
        Map<String, String> kmap = new HashMap<>();
        kmap.put("key", MTConfig.KEY);
        klist.add(kmap);
        auth.put(IntentoConfig.PROVIDER, klist);
        service.setAuth(auth);
        FromModel from = new FromModel(context, service);
        return from;
    }

    /**
     * get an id's translations from Intento, it uses async method which will pause one second in 5 retries, so at most
     * it will consume 5 seconds to get translations from Intento.
     *
     * @param id
     * @return
     * @throws MTException
     */
    private ToResult getAsyncTranslations(String id) throws MTException {
        String url = IntentoConfig.TRANSURL + "/" + id;
        Map<String, String> headers = new HashMap<>();
        headers.put("apikey", IntentoConfig.API_KEY);
        ObjectMapper mapper = new ObjectMapper();
        ToResult resultNode = null;
        try {
            int i = 0;
            while (i < IntentoConfig.RETRY) {
                String response = HTTPRequester.getData(url, "GET", headers);
                if (response != null && !response.equalsIgnoreCase("")) {
                    resultNode = mapper.readValue(response, com.vmware.vip.messages.mt.intento.ToResult.class);
                }
                if (resultNode == null || !resultNode.getDone()) {
                    i++;
                    Thread.sleep((IntentoConfig.INTERVAL/IntentoConfig.RETRY) * i);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            throw new MTException("Parsing MT response is failed!");
        }
        return resultNode;
    }
}