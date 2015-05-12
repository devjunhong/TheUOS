package com.uoscs09.theuos2.parse;

import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.StringUtil;

import java.net.HttpURLConnection;
import java.util.Map;

public class ParseUtil {
    public static <T> T parseXml(XmlParser<T> parser, String url, String paramsEncoding, Map<? extends CharSequence, ? extends CharSequence> params) throws Exception {
        HttpURLConnection connection = HttpRequest.getConnection(url, paramsEncoding, params);

        try {
            return parser.parse(connection.getInputStream());
        } finally {
            connection.disconnect();
        }

    }

    public static <T> T parseXml(XmlParser<T> parser, String url, Map<? extends CharSequence, ? extends CharSequence> params) throws Exception {
        return parseXml(parser, url,StringUtil.ENCODE_EUC_KR, params);

    }

    public static <T> T parseXml(XmlParser<T> parser, String url) throws Exception {
        return parseXml(parser, url,StringUtil.ENCODE_EUC_KR, null);

    }
}