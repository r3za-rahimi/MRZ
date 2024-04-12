package com.passport.mrz.service.parser.kind;

import com.passport.mrz.model.MrzInfo;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class MrzTd2Parser extends AbstractMrzParser {
	private final Pattern firstLinePattern =
			Pattern.compile(
					// document code, exclude V for visa
					"([A-UW-Z]{1}[A-Z0-9<]{1})" +
					// issuing state (ISO 3166-1 alpha-3 code with modifications)
					"([A-Z<]{3})" +
					// last name, then '<<', then first name(s)
					"([A-Z<]{31})");
	private final Pattern secondLinePattern =
			Pattern.compile(
					// document number
					"([A-Z0-9<]{9})" +
					// check digit over digits document number
					"([0-9<]{1})" +
					// nationality (ISO 3166-1 alpha-3 code with modifications)
					"([A-Z<]{3})" +
					// date of birth (YYMMDD)
					"([0-9<]{6})" +
					// check digit over digits date of birth
					"([0-9]{1})" +
					// sex (M, F or < for male, female or unspecified)
					"([MFX<]{1})" +
					// expiration date of passport (YYMMDD)
					"([0-9]{6})" +
					// check digit over digits expiration date
					"([0-9]{1})" +
					// optional data by the issuing state
					"([A-Z0-9<]{7})" +
					// check digit over digits passport number and check digit,
					// date of birth and check digit and date of expiration and
					// check digit
					"([0-9]{1})");

	public MrzInfo parse(String text) {
		String documentCode;
		String issuingState;
		String primaryIdentifier;
		String secondaryIdentifier;
		int endOfFirstLine;

		// first line
		{
			Matcher m = firstLinePattern.matcher(text);
			if (!m.find() || m.groupCount() < 3) {
				throw new IllegalArgumentException(
						MrzTd2Parser.class.getSimpleName() +
						": cannot parse line 1");
			}

			documentCode = m.group(1);
			issuingState = m.group(2);
			String names[] = splitName(m.group(3));
			primaryIdentifier = names[0];
			secondaryIdentifier = names[1];

			endOfFirstLine = m.end() - m.start();
		}

		String documentNumber;
		String documentNumberCheckDigit;
		String nationality;
		String dateOfBirth;
		String dateOfBirthCheckDigit;
		String sex;
		String dateOfExpiry;
		String dateOfExpiryCheckDigit;
		String optionalData;
		String checkDigit;

		// second line
		{
			Matcher m = secondLinePattern.matcher(text);
			if (!m.find(endOfFirstLine) || m.groupCount() < 10) {
				throw new IllegalArgumentException(
						MrzTd2Parser.class.getSimpleName() +
						": cannot parse line 2");
			}

			documentNumber = m.group(1);
			documentNumberCheckDigit = m.group(2);
			nationality = m.group(3);
			dateOfBirth = m.group(4);
			dateOfBirthCheckDigit = m.group(5);
			sex = m.group(6);
			dateOfExpiry = m.group(7);
			dateOfExpiryCheckDigit = m.group(8);
			optionalData = m.group(9);
			checkDigit = m.group(10);
		}

		if (!checkExtended(documentNumberCheckDigit, documentNumber, optionalData) ||
				!check(dateOfBirthCheckDigit, dateOfBirth) ||
				!check(dateOfExpiryCheckDigit, dateOfExpiry) ||
				!check(checkDigit, documentNumber +
						documentNumberCheckDigit +
						dateOfBirth +
						dateOfBirthCheckDigit +
						dateOfExpiry +
						dateOfExpiryCheckDigit +
						optionalData)) {
			throw new IllegalArgumentException(
					MrzTd2Parser.class.getSimpleName() +
					": checksum mismatch");
		}

		return new MrzInfo(
				documentCode,
				issuingState,
				primaryIdentifier,
				secondaryIdentifier,
				nationality,
				documentNumber,
				dateOfBirth,
				sex,
				dateOfExpiry);
	}
}
