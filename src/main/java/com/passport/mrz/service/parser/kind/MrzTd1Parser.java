package com.passport.mrz.service.parser.kind;

import com.passport.mrz.model.MrzInfo;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class MrzTd1Parser extends AbstractMrzParser {
	private  final Pattern firstLinePattern =
			Pattern.compile(
					// document code
					"([A-Z0-9<]{2})" +
					// issuing state (ISO 3166-1 alpha-3 code with modifications)
					"([A-Z<]{3})" +
					// document number
					"([A-Z0-9<]{9})" +
					// check digit over digits document number
					"([0-9<]{1})" +
					// optional data #1 by the issuing state
					"([A-Z0-9<]{15})");
	private  final Pattern secondLinePattern =
			Pattern.compile(
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
					// nationality (ISO 3166-1 alpha-3 code with modifications)
					"([A-Z<]{3})" +
					// optional data #2 by the issuing state
					"([A-Z0-9<]{11})" +
					// check digit over digits passport number and check digit,
					// date of birth and check digit and date of expiration and
					// check digit
					"([0-9]{1})");
	private static final Pattern thirdLinePattern =
			Pattern.compile(
					"([A-Z<]{30})");

	public MrzInfo parse(String text) {
		String documentCode;
		String issuingState;
		String documentNumber;
		String documentNumberCheckDigit;
		String optionalData1;
		int endOfLastLine;

		// first line
		{
			Matcher m = firstLinePattern.matcher(text);
			if (!m.find() || m.groupCount() < 5) {
				throw new IllegalArgumentException(
						MrzTd1Parser.class.getSimpleName() +
						": cannot parse line 1");
			}

			documentCode = m.group(1);
			issuingState = m.group(2);
			documentNumber = m.group(3);
			documentNumberCheckDigit = m.group(4);
			optionalData1 = m.group(5);

			endOfLastLine = m.end() - m.start();
		}

		String dateOfBirth;
		String dateOfBirthCheckDigit;
		String sex;
		String dateOfExpiry;
		String dateOfExpiryCheckDigit;
		String nationality;
		String optionalData2;
		String checkDigit;

		// second line
		{
			Matcher m = secondLinePattern.matcher(text);
			if (!m.find(endOfLastLine) || m.groupCount() < 8) {
				throw new IllegalArgumentException(
						MrzTd1Parser.class.getSimpleName() +
						": cannot parse line 2");
			}

			dateOfBirth = m.group(1);
			dateOfBirthCheckDigit = m.group(2);
			sex = m.group(3);
			dateOfExpiry = m.group(4);
			dateOfExpiryCheckDigit = m.group(5);
			nationality = m.group(6);
			optionalData2 = m.group(7);
			checkDigit = m.group(8);

			endOfLastLine = m.end() - m.start();
		}

		if (!checkExtended(documentNumberCheckDigit, documentNumber, optionalData1) ||
				!check(dateOfBirthCheckDigit, dateOfBirth) ||
				!check(dateOfExpiryCheckDigit, dateOfExpiry) ||
				!check(checkDigit, documentNumber +
						documentNumberCheckDigit +
						optionalData1 +
						dateOfBirth +
						dateOfBirthCheckDigit +
						dateOfExpiry +
						dateOfExpiryCheckDigit +
						optionalData2)) {
			throw new IllegalArgumentException(
					MrzTd1Parser.class.getSimpleName() +
					": checksum mismatch");
		}

		String primaryIdentifier;
		String secondaryIdentifier;

		// third line
		{
			Matcher m = thirdLinePattern.matcher(text);
			if (!m.find(endOfLastLine) || m.groupCount() < 1) {
				throw new IllegalArgumentException(
						MrzTd1Parser.class.getSimpleName() +
						": cannot parse line 3");
			}

			String names[] = splitName(m.group(1));
			primaryIdentifier = names[0];
			secondaryIdentifier = names[1];
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
