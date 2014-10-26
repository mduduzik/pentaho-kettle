package org.pentaho.di.www.ge.util;

import java.nio.charset.CharacterCodingException;

public class HTTPUtil {

	public String decodeURL(String url) throws CharacterCodingException {
		return decodeURL(url, false);
	}

	public String decodeURL(String url, boolean unescapeSpaces) throws CharacterCodingException {
		return URLCodec.decodeURL(url, StringPool.UTF8, unescapeSpaces);
	}


	public static String encodeURL(String url) throws CharacterCodingException {
		return encodeURL(url, false);
	}

	public static String encodeURL(String url, boolean escapeSpaces) throws CharacterCodingException {
		return URLCodec.encodeURL(url, StringPool.UTF8, escapeSpaces);
	}

	public static String fixPath(String path) {
		return fixPath(path, true, true);
	}

	public static String fixPath(String path, boolean leading, boolean trailing) {
		if (path == null) {
			return StringPool.BLANK;
		}

		int leadingSlashCount = 0;
		int trailingSlashCount = 0;

		if (leading) {
			for (int i = 0; i < path.length(); i++) {
				if (path.charAt(i) == CharPool.SLASH) {
					leadingSlashCount++;
				}
				else {
					break;
				}
			}
		}

		if (trailing) {
			for (int i = path.length() - 1; i >=0; i--) {
				if (path.charAt(i) == CharPool.SLASH) {
					trailingSlashCount++;
				}
				else {
					break;
				}
			}
		}

		int slashCount = leadingSlashCount + trailingSlashCount;

		if (slashCount > path.length()) {
			return StringPool.BLANK;
		}

		if (slashCount > 0) {
			path = path.substring(
				leadingSlashCount, path.length() - trailingSlashCount);
		}

		return path;
	}

}
