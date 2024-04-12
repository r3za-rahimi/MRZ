package com.passport.mrz.service.parser.kind;

public abstract class AbstractMrzParser {
	private static final int CHECK_DIGIT_WEIGHTS[] = new int[]{7, 3, 1};

	public static int calculateCheckSum(String s) {
		int sum = 0;

		for (int i = 0, len = s.length(); i < len; ++i) {
			byte ch = (byte) s.charAt(i);

			if (ch > 64 && ch < 91) {
				// map A-Z to 10-35
				ch -= 55;
			} else if (ch > 47 && ch < 58) {
				// use number value
				ch -= 48;
			} else if (ch == 60) {
				// '<' is 0
				ch = 0;
			} else {
				// invalid character
				return -1;
			}

			sum += ch * CHECK_DIGIT_WEIGHTS[
					i % CHECK_DIGIT_WEIGHTS.length];
		}

		return sum % 10;
	}

	protected static String[] splitName(String text) {
		// Make sure to return an array with at least two items.
		String names[] = text.split("<<");
		return new String[]{
				names[0],
				names.length < 2 ? "" : names[1]};
	}

	protected static boolean checkExtended(
			String checkDigit,
			String text,
			String extension) {
		// Check for an extended document number.
		String alt = null;
		if ("<".equals(checkDigit) &&
				extension != null && extension.length() > 1) {
			int p = extension.indexOf("<");
			if (p > 1) {
				--p;
				checkDigit = extension.substring(p, p + 1);
				String ext = extension.substring(0, p);
				// Unfortunately, Note j in ICAO 9303p5 doesn't specify
				// if the `<` shall be part of the checksum calculation or
				// not. This means some issuers include the `<` and other
				// don't so we need to accept both variants.
				alt = text + ext;
				text += "<" + ext;
			}
		}
		return check(checkDigit, text) ||
				(alt != null && check(checkDigit, alt));
	}

	protected static boolean check(String checkDigit, String text) {
		return checkDigit != null &&
				checkDigit.length() == 1 &&
				text != null &&
				calculateCheckSum(text) == valueOf(checkDigit.charAt(0));
	}

	private static int valueOf(char ch) {
		// '0' = 0 to '9' = 9
		return ch == '<' ? 0 : (int) ch - 48;
	}
}
