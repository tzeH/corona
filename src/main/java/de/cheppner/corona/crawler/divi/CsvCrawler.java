package de.cheppner.corona.crawler.divi;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class CsvCrawler {

	public static void main(String[] args) {
		LocalDate current = LocalDate.now();

		if (LocalTime.now().isBefore(LocalTime.of(13, 0))) {
			current = current.minusDays(1);
		}

		Set<LocalDate> fehlendeDaten = Set.of(//
				LocalDate.of(2020, 9, 14), // fehlt
				LocalDate.of(2020, 11, 1) // pdf kaputt
		);

		try {
			// Danach haben sich die Download-Links geändert
			while (current.isAfter(LocalDate.of(2020, 6, 4))) {
				boolean downloaded = false;
				downloaded |= downloadCsvForDate(current);
				downloaded |= downloadPdfForDate(current);

				do {
					current = current.minusDays(1);
				} while (fehlendeDaten.contains(current));

				if (downloaded) {
					Thread.sleep((long) (1000 + Math.random() * 1000));
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static boolean downloadCsvForDate(LocalDate current) {
//		https://www.divi.de/divi-intensivregister-tagesreport-archiv-csv/viewdocument/3720/divi-intensivregister-2020-06-04-09-15
		String baseUrl = "https://www.divi.de/joomlatools-files/docman-files/divi-intensivregister-tagesreports-csv/";
		String filenameFormat = "'DIVI-Intensivregister_'yyyy-MM-dd'_12-15.csv'";

		return downloadFileForDate(current, baseUrl, filenameFormat, filenameFormat);
	}

	private static boolean downloadPdfForDate(LocalDate current) {
		String baseUrl = "https://www.divi.de/joomlatools-files/docman-files/divi-intensivregister-tagesreports/";
		String filenameFormat = "'DIVI-Intensivregister_Tagesreport_'yyyy_MM_dd'.pdf'";
		String targetFilenameFormat = "'DIVI-Intensivregister_Tagesreport_'yyyy_MM_dd'.pdf'";

		return downloadFileForDate(current, baseUrl, filenameFormat, targetFilenameFormat);
	}

	private static boolean downloadFileForDate(LocalDate current, String baseUrl, String filenameFormat,
			String targetFilenameFormat) {
		String sourceFileName = current.format(DateTimeFormatter.ofPattern(filenameFormat));
		String targetFilename = current.format(DateTimeFormatter.ofPattern(targetFilenameFormat));

		try {
			Path targetPath = Paths.get("data/divi/", targetFilename);

			if (Files.exists(targetPath)) {
				System.out.println("Skipping existing " + targetFilename);
				return false;
			}

			System.out.println("Downloading " + baseUrl + sourceFileName);

			URL url = new URL(baseUrl + sourceFileName);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			byte[] readStream = readStream(con.getInputStream());

			Files.write(targetPath, readStream, StandardOpenOption.CREATE);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static byte[] readStream(InputStream in) {
		try {
			return in.readAllBytes();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
