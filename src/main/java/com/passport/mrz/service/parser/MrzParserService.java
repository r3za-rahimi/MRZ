package com.passport.mrz.service.parser;

import com.passport.mrz.model.MrzInfo;
import com.passport.mrz.service.parser.kind.*;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class MrzParserService {

	private final MrzTd1Parser mrzTd1Parser;
	private final MrzFranceParser mrzFranceParser;
	private final MrzMrvBParser mrzMrvBParser;
	private final MrzTd2Parser mrzTd2Parser;
	private final MrzMrvAParser mrzMrvAParser;
	private final MrzTd3Parser mrzTd3Parser;



	private  final Pattern invalidCharacters = Pattern.compile(
			"[^A-Z0-9<]");

    public MrzParserService(MrzTd1Parser mrzTd1Parser, MrzFranceParser mrzFranceParser, MrzMrvBParser mrzMrvBParser, MrzTd2Parser mrzTd2Parser, MrzMrvAParser mrzMrvAParser, MrzTd3Parser mrzTd3Parser) {
        this.mrzTd1Parser = mrzTd1Parser;
        this.mrzFranceParser = mrzFranceParser;
        this.mrzMrvBParser = mrzMrvBParser;
        this.mrzTd2Parser = mrzTd2Parser;
        this.mrzMrvAParser = mrzMrvAParser;
        this.mrzTd3Parser = mrzTd3Parser;
    }

    public  String purify(String text) {
		return invalidCharacters.matcher(text).replaceAll("");
	}

	public  MrzInfo parse(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text cannot be null");
		}
		switch (text.length()) {
			case 90:
				return mrzTd1Parser.parse(text);
			case 72:
				if (text.startsWith("IDFRA")) {
					return mrzFranceParser.parse(text);
				}
				return text.startsWith("V") ?
						mrzMrvBParser.parse(text) :
						mrzTd2Parser.parse(text);
			case 88:
				return text.startsWith("V") ?
						mrzMrvAParser.parse(text) :
						mrzTd3Parser.parse(text);
			default:
				throw new IllegalArgumentException("invalid length");
		}
	}


}
