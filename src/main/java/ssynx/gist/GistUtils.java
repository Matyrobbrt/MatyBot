package ssynx.gist;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import matyrobbrt.matybot.util.BotUtils;

public class GistUtils {

	private static int lastCode = 0;
	private static String lastErrorMessage = "";

	public static Gist create(final String token, final BetterGist gist) throws JaGistException {
		String newGist;
		try {
			newGist = post(token, "", gist.toString());
		} catch (IOException ioe) {
			int code = lastCode;
			if (code == 404)
				return null;

			throw new JaGistException(lastErrorMessage, lastCode);
		}

		return new Gist(newGist);
	}

	private static String post(final String token, final String operation, final String what) throws IOException {
		final URL target = new URL("https://api.github.com/gists" + operation);
		final HttpsURLConnection connection = (HttpsURLConnection) target.openConnection();

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Authorization", "token " + token);

		OutputStream ost = connection.getOutputStream();
		try (final DataOutputStream requestBody = new DataOutputStream(ost)) {
			requestBody.writeBytes(what);
		} finally {
			ost.close();
		}

		String res;
		try {
			res = getResponse(connection.getInputStream());
		} finally {
			assignLast(connection);
		}

		return res;
	}

	private static String getResponse(final InputStream stream) throws IOException {
		StringBuilder full = new StringBuilder();
		String line;

		InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		try (BufferedReader streamBuf = new BufferedReader(reader)) {

			while ((line = streamBuf.readLine()) != null)
				full.append(line);

		} finally {
			reader.close();
			stream.close();
		}

		return full.toString();
	}

	private static void assignLast(HttpsURLConnection conn) {
		try {
			lastCode = conn.getResponseCode();
			lastErrorMessage = conn.getResponseMessage();
		} catch (IOException ioe) {
			lastCode = -1;
			lastErrorMessage = "Unknown";
		}
	}

	public static boolean hasToken() {
		if (BotUtils.getGithubToken() == null) {
			return false;
		} else {
			return !BotUtils.getGithubToken().isBlank();
		}
	}

}
