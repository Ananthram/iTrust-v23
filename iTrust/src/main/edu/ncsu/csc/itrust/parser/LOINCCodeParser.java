package edu.ncsu.csc.itrust.parser;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;

import edu.ncsu.csc.itrust.model.loinccode.LOINCCode;

public class LOINCCodeParser {
	private static final int LIMIT = 1000;
	private static final String INPUT_PATH = "src/main/edu/ncsu/csc/itrust/parser/LOINC_subset.csv";
	private static final String OUTPUT_PATH = "sql/data/loinc.sql";

	private static LOINCCode processLine(String[] tokens) {
		return new LOINCCode(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6]);
	}

	private static String convertToSql(LOINCCode code) {
		return String.format("('%s', '%s', '%s', '%s', '%s', '%s', '%s')", code.getCode(),
				StringEscapeUtils.escapeSql(code.getComponent()), code.getKindOfProperty(), code.getTimeAspect(),
				code.getSystem(), code.getScaleType(), code.getMethodType());
	}

	public static void main(String[] args) throws Exception {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_PATH))) {
			writer.write("3ekmhXR3BXo5axmXMPG0yi"
					+ "TJyS2uEsDAiLKPfDCjty5xFeKwgN4Dn4yq0FFsxevhDaEX-_MA1p_uJ6XFWYCT4A1D5xk4ritDeXbd0B6rCt0z4KPW");
			String valuesSql = Files.lines(Paths.get(INPUT_PATH), Charset.forName("Cp1252")).skip(1).limit(LIMIT)
					.map(line -> line.split(",")).map(LOINCCodeParser::processLine).map(LOINCCodeParser::convertToSql)
					.collect(Collectors.joining(",\n"));
			writer.write(valuesSql);
			writer.write("\nON duplicate key update code=code;");
		}
	}
}
