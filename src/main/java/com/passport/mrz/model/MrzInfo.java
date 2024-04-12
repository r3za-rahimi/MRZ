package com.passport.mrz.model;

import com.passport.mrz.service.parser.kind.AbstractMrzParser;

public class MrzInfo {
	public final String documentCode;
	public final String issuingState;
	public final String primaryIdentifier;
	public final String secondaryIdentifier;
	public final String nationality;
	public final String documentNumber;
	public final String dateOfBirth;
	public final String sex;
	public final String dateOfExpiry;

	public MrzInfo(
			String documentCode,
			String issuingState,
			String primaryIdentifier,
			String secondaryIdentifier,
			String nationality,
			String documentNumber,
			String dateOfBirth,
			String sex,
			String dateOfExpiry) {
		this.documentCode = unpad(documentCode);
		this.issuingState = unpad(issuingState);
		this.primaryIdentifier = unpad(primaryIdentifier);
		this.secondaryIdentifier = unpad(secondaryIdentifier);
		this.nationality = unpad(nationality);
		this.documentNumber = unpad(documentNumber);
		this.dateOfBirth = unpad(dateOfBirth);
		this.sex = unpad(sex);
		this.dateOfExpiry = unpad(dateOfExpiry);
	}

	public String toString() {
		// Return td-1 format (without optional data).
		String firstLine = pad(30,
				pad(2, documentCode) +
				pad(3, issuingState) +
				pad(9, documentNumber, "0") +
				AbstractMrzParser.calculateCheckSum(documentNumber));
		String secondLine = pad(29,
				pad(6, dateOfBirth) +
				AbstractMrzParser.calculateCheckSum(dateOfBirth) +
				pad(1, sex) +
				pad(6, dateOfExpiry) +
				AbstractMrzParser.calculateCheckSum(dateOfExpiry) +
				pad(3, nationality));
		secondLine += AbstractMrzParser.calculateCheckSum(
				firstLine.substring(5, 30) +
				secondLine.substring(0, 7) +
				secondLine.substring(8, 15) +
				secondLine.substring(18, 29));
		String thirdLine = pad(30,
				(primaryIdentifier + "<<" + secondaryIdentifier).replace(
						" ", "<"));
		return firstLine + secondLine + thirdLine;
	}

	private static String unpad(String text) {
		return text == null ? "" : text.replace("<", " ").trim();
	}

	private static String pad(int min, String text) {
		return pad(min, text, "<");
	}

	private static String pad(int min, String text, String filler) {
		if (text == null) {
			text = "";
		}
		int len = text.length();
		if (len < min) {
			text += new String(new char[min - len]).replace("\0", filler);
		}
		return text.substring(0, min);
	}
}
