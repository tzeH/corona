package de.cheppner.corona.crawler.divi;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfExtractor {
	public static void main(String[] args) throws IOException {
		Path dataFolder = Paths.get("data/divi");

		System.out.println("Datei\tAktuell in Behandlung\tNeuaufnahmen auf ITS seit Vortag\tabgeschlossene Behandlungen\tVerstorben auf ITS");

		Files.list(dataFolder).filter(p -> {
			return p.toString().endsWith("pdf");
		}).forEachOrdered(PdfExtractor::parsePdf);

	}

	private static void parsePdf(Path path) {
		try {
			PDDocument document = PDDocument.load(path.toFile());

			PDFTextStripper pdfStripper = new PDFTextStripper();
			pdfStripper.setStartPage(1);
			pdfStripper.setEndPage(1);
			String parsedText = pdfStripper.getText(document);

			extractNumber(path.getFileName().toString(), parsedText);

			document.close();
		} catch (IOException e) {
			System.err.println("Error parsing pdf " + path);
			e.printStackTrace();
		}
	}

	private static void extractNumber(String filename, String parsedText) {
		Matcher behandelt = Pattern.compile("Aktuell in intensivmedizinischer Behandlung +([0-9.]+) +([0-9-+.]+)")
				.matcher(parsedText);
		Matcher neuaufnahmen = Pattern
				.compile("Neuaufnahmen (auf ITS seit Vortag|\\(inkl\\. Verlegungen\\*\\)) +([0-9.+-]+)")
				.matcher(parsedText);
		Matcher abgeschlossen = Pattern.compile("(" //
				+ "abgeschlossene Behandlungen " // ?? - 8.9.2020
				+ "|" //
				+ "abgeschlossener Behandlung " // 9.9.2020-3.3.2021
				+ "|" //
				// ab 4.3.2021
				+ "Gesamt Abgeschlossene ITS-Behandlungen\r\n\\(durch Genesen, Versterben, ITS-Verlegung\\*\\)\r\n"
				+ ")"//
				+ "([0-9.+-]+)").matcher(parsedText);
		Matcher tote = Pattern.compile("(" //
				+ "davon verstorben [0-9.]+ \\([0-9]+%\\) " // ?? - 22.10.2020
				+ "|" //
				// ab 4.3.2021
				+ "Verstorben auf ITS "//
				+ ")" + "([0-9.+-]+)").matcher(parsedText);

		String behandeltStr = behandelt.find() ? behandelt.group(1) : "";
		String neuaufnahmenStr = neuaufnahmen.find() ? neuaufnahmen.group(2) : "";
		String abgeschlossenStr = abgeschlossen.find() ? abgeschlossen.group(2) : "";
		String toteStr = tote.find() ? tote.group(2) : "";

		System.out.println(
				filename + "\t" + behandeltStr + "\t" + neuaufnahmenStr + "\t" + abgeschlossenStr + "\t" + toteStr);

//		if (behandeltStr.isEmpty() || neuaufnahmenStr.isEmpty() || abgeschlossenStr.isEmpty() || toteStr.isEmpty()) {
//			System.out.println(parsedText);
//		}
	}
}
