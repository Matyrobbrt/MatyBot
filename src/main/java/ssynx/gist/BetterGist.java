package ssynx.gist;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

public class BetterGist {

	private final String desc;
	private final boolean isPublic;
	private final Map<String, JSONObject> files = new LinkedHashMap<>();

	public BetterGist(final String description, final boolean isPublic) {
		desc = description;
		this.isPublic = isPublic;
	}

	public BetterGist(final String description, final boolean isPublic, final String filename, final String content) {
		desc = description;
		this.isPublic = isPublic;

		final JSONObject fileobj = new JSONObject();
		fileobj.put("content", content);

		files.put(filename, fileobj);
	}

	public BetterGist(final String description, final boolean isPublic, final File file) throws IOException {
		desc = description;
		this.isPublic = isPublic;

		final JSONObject fileobj = new JSONObject();
		fileobj.put("content", JaGistHttps.readFile(file));

		files.put(file.getName(), fileobj);
	}

	public BetterGist addFile(final String filename, final String content) {
		final JSONObject fileobj = new JSONObject();
		fileobj.put("content", content);

		files.put(filename, fileobj);
		return this;
	}

	public BetterGist addFile(final File file) throws IOException {
		final JSONObject fileobj = new JSONObject();
		fileobj.put("content", JaGistHttps.readFile(file));

		files.put(file.getName(), fileobj);
		return this;
	}

	@Override
	public String toString() {
		JSONObject gistobj = new JSONObject();
		JSONObject fileobj = new JSONObject();

		gistobj.put("public", isPublic);
		gistobj.put("description", desc);

		for (final Map.Entry<String, JSONObject> entry : files.entrySet()) {
			fileobj.put(entry.getKey(), entry.getValue());
		}

		gistobj.put("files", fileobj);

		return gistobj.toString();
	}

}
