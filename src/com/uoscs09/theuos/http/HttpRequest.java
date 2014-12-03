package com.uoscs09.theuos.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.uoscs09.theuos.common.util.StringUtil;

public class HttpRequest {
	public static String getBody(String url, String encoding,
			Map<String, String> params) throws SocketTimeoutException,
			UnsupportedEncodingException, IOException {
		return getBody(url, encoding, params, encoding);
	}

	public static String getBody(String url, String encoding,
			Map<String, String> params, String paramsEncoding)
			throws SocketTimeoutException, UnsupportedEncodingException,
			IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(url).append('?');
		encodeString(sb, params, paramsEncoding);
		return getBody(sb.toString(), encoding);
	}

	public static String getBodyByPost(String url, String encoding,
			Map<String, String> params, String paramsEncoding)
			throws SocketTimeoutException, UnsupportedEncodingException,
			IOException {
		StringBuilder sb = new StringBuilder();
		encodeString(sb, params, paramsEncoding);
		return getBodyByPost(url, sb.toString(), encoding);
	}

	public static String getBody(String url, Map<String, String> params)
			throws SocketTimeoutException, UnsupportedEncodingException,
			IOException {
		return getBody(url, StringUtil.ENCODE_UTF_8, params);
	}

	public static StringBuilder encodeString(StringBuilder b,
			Map<String, String> table, String encoding)
			throws UnsupportedEncodingException {
		final char eq = '=', amp = '&';
		for (Entry<String, String> entry : table.entrySet()) {
			b.append(URLEncoder.encode(entry.getKey(), encoding)).append(eq)
					.append(URLEncoder.encode(entry.getValue(), encoding))
					.append(amp);
		}
		b.deleteCharAt(b.length() - 1);
		return b;
	}

	public static StringBuilder encodeString(StringBuilder b,
			Map<String, String> params)
			throws UnsupportedEncodingException {
		return encodeString(b, params, StringUtil.ENCODE_UTF_8);
	}

	public static String getBody(String url) throws IOException,
			SocketTimeoutException {
		return getBody(url, StringUtil.ENCODE_UTF_8);
	}

	public static String getBody(String url, String encoding)
			throws IOException, SocketTimeoutException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();
		try {
			if (connection == null) {
				throw new IOException("HttpURLConnection returns null.");
			}

			connection.setConnectTimeout(5000);

			checkResponseAndThrowException(connection);

			return readContentFromStream(connection.getInputStream(), encoding);
		} finally {
			connection.disconnect();
		}
	}

	public static String getBodyByPost(String url, String params,
			String encoding) throws IOException, SocketTimeoutException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();
		try {
			if (connection == null) {
				throw new IOException("HttpURLConnection returns null.");
			}

			connection.setConnectTimeout(5000);
			connection.setDefaultUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("content-type",
					"application/x-www-form-urlencoded");

			PrintWriter pw = new PrintWriter(new OutputStreamWriter(
					connection.getOutputStream()));
			pw.write(params);
			pw.flush();

			checkResponseAndThrowException(connection);

			return readContentFromStream(connection.getInputStream(), encoding);
		} finally {
			connection.disconnect();
		}
	}

	static final void checkResponseAndThrowException(
			HttpURLConnection connection) throws IOException {
		int response = connection.getResponseCode();
		if (response != HttpURLConnection.HTTP_OK) {
			Log.e("HttpRequest", connection.getURL().toExternalForm()
					+ " / response : " + response);
			throw new IOException("HttpURLConnection responses bad result.");
		}
	}

	static final String readContentFromStream(InputStream in,
			String encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in,
				encoding));
		StringBuilder builder = new StringBuilder();
		String line = null;
		final char c = '\n';
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line).append(c);
			}
			reader.close();
			return builder.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

}
