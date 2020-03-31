package de.cheppner.corona.simulator;

import java.time.ZonedDateTime;

public class RkiEntry {
	public String getBundesland() {
		return Bundesland;
	}

	public String getLandkreis() {
		return Landkreis;
	}

	public String getAltersgruppe() {
		return Altersgruppe;
	}

	public String getGeschlecht() {
		return Geschlecht;
	}

	public String getIdLandkreis() {
		return IdLandkreis;
	}

	public ZonedDateTime getMeldedatum() {
		return Meldedatum;
	}

	public Integer getAnzahlFall() {
		return AnzahlFall;
	}

	public Integer getAnzahlTodesfall() {
		return AnzahlTodesfall;
	}

	private String Bundesland, Landkreis, Altersgruppe, Geschlecht, IdLandkreis;
	private ZonedDateTime Meldedatum;
	private Integer AnzahlFall, AnzahlTodesfall;

	public static RkiEntry fromCsv(String line) {
		String[] felder = line.split(",");

		RkiEntry result = new RkiEntry();
		result.Bundesland = felder[1];
		result.Landkreis = felder[2];
		result.Altersgruppe = felder[3];
		result.Geschlecht = felder[4];
		result.AnzahlFall = Integer.parseInt(felder[5]);
		result.AnzahlTodesfall = Integer.parseInt(felder[6]);
		result.Meldedatum = ZonedDateTime.parse(felder[8]);
		result.IdLandkreis = felder[9];

		return result;
	}

	@Override
	public String toString() {
		return "RkiEntry [Bundesland=" + Bundesland + ", Landkreis=" + Landkreis + ", Altersgruppe=" + Altersgruppe
				+ ", Geschlecht=" + Geschlecht + ", IdLandkreis=" + IdLandkreis + ", Meldedatum=" + Meldedatum
				+ ", AnzahlFall=" + AnzahlFall + ", AnzahlTodesfall=" + AnzahlTodesfall + "]";
	}

}
