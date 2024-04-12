package com.passport.mrz.service.parser.kind;

import com.passport.mrz.model.MrzInfo;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MrzFranceParser extends AbstractMrzParser {
	// France got its very own MRZ on ID cards:
	// https://en.wikipedia.org/wiki/National_identity_card_(France)
	private  final Pattern firstLinePattern =
			Pattern.compile(
					// document code and type
					"(I[A-Z0-9<]{1})" +
					// nationality (ISO 3166-1 alpha-3 code with modifications)
					"(FRA)" +
					// last name followed by '<' symbols
					"([A-Z<]{25})" +
					// digits 5-7 of ID card number, department of issuance
					"([A-Z0-9<]{3})" +
					// office of issuance
					"([0-9<]{3})");
	private  final Pattern secondLinePattern =
			Pattern.compile(
					// date of issuance (YYMM)
					"([0-9]{2})" +
					"([0-9]{2})" +
					// department of issuance, same as characters 31-33 on
					// the first line
					"([A-Z0-9<]{3})" +
					// assigned by the Management Center in chronological
					// order in relation to the place of issue and the date
					// of application
					"([0-9]{5})" +
					// check digit over digits 1–12
					"([0-9]{1})" +
					// first name followed by given names separated by
					// two filler characters
					"([A-Z<]{14})" +
					// date of birth (YYMMDD)
					"([0-9<]{6})" +
					// check digit over date of birth
					"([0-9]{1})" +
					// sex (M, F or < for male, female or unspecified)
					"([MFX<]{1})" +
					// check digit over digits 1-36 in first row combined
					// with digits 1-35 in second row
					"([0-9]{1})");

	public  MrzInfo parse(String text) {
		String documentCode;
		String nationality;
		String primaryIdentifier;
		String departmentOfIssuance;
		String officeOfIssuance;
		int endOfFirstLine;

		// first line
		{
			Matcher m = firstLinePattern.matcher(text);
			if (!m.find() || m.groupCount() < 5) {
				throw new IllegalArgumentException(
						MrzFranceParser.class.getSimpleName() +
						": cannot parse line 1");
			}

			documentCode = m.group(1);
			nationality = m.group(2);
			primaryIdentifier = m.group(3);
			departmentOfIssuance = m.group(4);
			officeOfIssuance = m.group(5);

			endOfFirstLine = m.end() - m.start();
		}

		String yearOfIssuance;
		String monthOfIssuance;
		String departmentOfIssuance2;
		String documentNumber;
		String documentNumberCheckDigit;
		String secondaryIdentifier;
		String dateOfBirth;
		String dateOfBirthCheckDigit;
		String sex;
		String checkDigit;

		// second line
		{
			Matcher m = secondLinePattern.matcher(text);
			if (!m.find(endOfFirstLine) || m.groupCount() < 10) {
				throw new IllegalArgumentException(
						MrzFranceParser.class.getSimpleName() +
						": cannot parse line 2");
			}

			yearOfIssuance = m.group(1);
			monthOfIssuance = m.group(2);
			departmentOfIssuance2 = m.group(3);
			documentNumber = m.group(4);
			documentNumberCheckDigit = m.group(5);
			secondaryIdentifier = m.group(6);
			dateOfBirth = m.group(7);
			dateOfBirthCheckDigit = m.group(8);
			sex = m.group(9);
			checkDigit = m.group(10);
		}

		if (!check(documentNumberCheckDigit, yearOfIssuance +
						monthOfIssuance +
						departmentOfIssuance2 +
						documentNumber) ||
				!check(dateOfBirthCheckDigit, dateOfBirth) ||
				!check(checkDigit, documentCode +
						nationality +
						primaryIdentifier +
						departmentOfIssuance +
						officeOfIssuance +
						yearOfIssuance +
						monthOfIssuance +
						departmentOfIssuance2 +
						documentNumber +
						documentNumberCheckDigit +
						secondaryIdentifier +
						dateOfBirth +
						dateOfBirthCheckDigit +
						sex)) {
			throw new IllegalArgumentException(
					MrzFranceParser.class.getSimpleName() +
					": checksum mismatch");
		}

		return new MrzInfo(
				documentCode,
				nationality,
				primaryIdentifier,
				secondaryIdentifier,
				nationality,
				documentNumber,
				dateOfBirth,
				sex,
				getDateOfExpiry(yearOfIssuance, monthOfIssuance));
	}

	private  String getDateOfExpiry(String yearOfIssuance,
			String monthOfIssuance) {
		int year;
		try {
			year = Integer.parseInt(yearOfIssuance);
		} catch (NumberFormatException e) {
			return null;
		}
		// Add 10 years if the ID card was issued before 2014, but 15 if
		// it was issued in or after 2014. Unfortunately, only the last
		// two digits of a year are known, so we can't distiguish between
		// 1925 and 2025. Let's just say everything greater than now is
		// a year of the past millenium.
		return String.format("%02d%2s01",
				(year + (year < 14 || year > 18 ? 10 : 15)) % 100,
				monthOfIssuance);
	}
}
