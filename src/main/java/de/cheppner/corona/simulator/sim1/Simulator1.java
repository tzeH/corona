package de.cheppner.corona.simulator.sim1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.cheppner.corona.simulator.Landkreis;
import de.cheppner.corona.simulator.RkiEntry;

public class Simulator1 {

	private List<Zeitschritt> schritte;
	private Map<String, Landkreis> landkreise;

	public Simulator1(List<RkiEntry> rkiDaten, Map<String, Landkreis> landkreise) {

		this.landkreise = landkreise;
		Map<LocalDate, List<RkiEntry>> entriesNachDatum = new TreeMap<>();

		LocalDate erstesDatum = LocalDate.now();
		LocalDate letztesDatum = LocalDate.of(2020, 1, 1);
		Set<Kohorte> alleKohorten = new HashSet<>();

		for (RkiEntry rkiEntry : rkiDaten) {
			LocalDate meldedatum = rkiEntry.getMeldedatum().toLocalDate();
			if (!entriesNachDatum.containsKey(meldedatum)) {
				entriesNachDatum.put(meldedatum, new ArrayList<RkiEntry>());
			}
			entriesNachDatum.get(meldedatum).add(rkiEntry);

			if (meldedatum.isBefore(erstesDatum)) {
				erstesDatum = meldedatum;
			}
			if (meldedatum.isAfter(letztesDatum)) {
				letztesDatum = meldedatum;
			}
			alleKohorten.add(createKohorte(rkiEntry));
		}

		int tageBisDiagnose = 10;
		erstesDatum = erstesDatum.minusDays(tageBisDiagnose);

		// Fehlende Tage auffüllen
		for (LocalDate tag = erstesDatum.minusDays(tageBisDiagnose); tag
				.isBefore(letztesDatum); tag = tag.plusDays(1)) {
			if (!entriesNachDatum.containsKey(tag)) {
				entriesNachDatum.put(tag, new ArrayList<RkiEntry>());
			}
		}

		schritte = new ArrayList<Zeitschritt>();
		for (Entry<LocalDate, List<RkiEntry>> entry : entriesNachDatum.entrySet()) {
			ArrayList<Kohortenschritt> kohorten = new ArrayList<Kohortenschritt>();
			LocalDate meldedatum = entry.getKey();
			schritte.add(new Zeitschritt(meldedatum, kohorten));

			Map<Kohorte, List<RkiEntry>> entriesNachKohorte = new HashMap<>();
			for (RkiEntry rkiEntry : entry.getValue()) {
				Kohorte kohorte = createKohorte(rkiEntry);

				if (!entriesNachKohorte.containsKey(kohorte)) {
					entriesNachKohorte.put(kohorte, new ArrayList<>());
				}
				entriesNachKohorte.get(kohorte).add(rkiEntry);
			}

			for (Entry<Kohorte, List<RkiEntry>> entry2 : entriesNachKohorte.entrySet()) {
				Kohorte kohorte = entry2.getKey();

				int neueFaelle = 0;
				int neueTote = 0;
				for (RkiEntry rkiEntry : entry2.getValue()) {
					neueFaelle += rkiEntry.getAnzahlFall();
					neueTote += rkiEntry.getAnzahlTodesfall();
				}

				kohorten.add(new Kohortenschritt(meldedatum, kohorte, neueFaelle, neueTote));
			}

			for (Kohorte kohorte : alleKohorten) {
				if (!entriesNachKohorte.containsKey(kohorte)) {
					kohorten.add(new Kohortenschritt(meldedatum, kohorte, 0, 0));
				}
			}

			kohorten.sort(Comparator.comparing(Kohortenschritt::getKohorte));
		}

	}

	public void run() {
		Map<Kohorte, Kohortenschritt> letzterStand = new TreeMap<>();
		printHeaders(schritte.get(0).kohorten);
		for (Zeitschritt schritt : schritte) {
			List<Kohortenschritt> kohorten = schritt.kohorten;

			for (Kohortenschritt kohortenschritt : kohorten) {
				Kohorte kohorte = kohortenschritt.kohorte;
				kohortenschritt.einwohner = getEinwohnerZahl(kohorte);

				if (letzterStand.containsKey(kohortenschritt.kohorte)) {
					updateModel(kohortenschritt, letzterStand.get(kohorte));
				} else {
					kohortenschritt.infected = kohortenschritt.neueFaelle;
					kohortenschritt.suspectible = kohortenschritt.einwohner;
				}
				letzterStand.put(kohorte, kohortenschritt);
			}
			printValues(kohorten);
		}

	}

	private int getEinwohnerZahl(Kohorte kohorte) {
		if (kohorte.landkreis.equals("SK Kempten")) {
			return 68330; // https://www.google.com/search?client=firefox-b-d&q=einwohner+Kempten
		}
//		return 10000000;

		return landkreise.values().stream().filter(lk -> lk.getBundesland().equals(kohorte.landkreis))
				.mapToInt(Landkreis::getEinwohnerzahl).sum();

//		return landkreise.get(kohorte.landkreis).getEinwohnerzahl();
	}

	private void updateModel(Kohortenschritt neu, Kohortenschritt alt) {
		neu.gesamtFaelle = alt.gesamtFaelle + neu.neueFaelle;
		neu.gesamtTode = alt.gesamtTode + neu.neueTode;
		neu.deltaSteigung = neu.neueFaelle / (double) alt.neueFaelle;

		double heilungsrate = 0.1;
		double ansteckungsrate = 1.38;

		if (neu.meldedatum.isAfter(LocalDate.of(2020, 3, 19))) {
			ansteckungsrate = 1.23;
		}

		if (neu.meldedatum.isAfter(LocalDate.of(2020, 3, 24))) {
			ansteckungsrate = 1.16;
		}

		ansteckungsrate = 1.16;

		double neuRemoved = alt.infected * heilungsrate;
		neu.infected = alt.infected * ansteckungsrate * alt.suspectible / alt.einwohner - neuRemoved;

		if (neu.infected < neu.gesamtFaelle) {
			neu.infected = neu.gesamtFaelle;
		}

		neu.removed += neuRemoved;
		neu.suspectible = neu.einwohner - neu.infected - neu.removed;
		neu.newInfected = neu.infected - alt.infected;

	}

	private boolean includeKohorte(Kohortenschritt k) {
//		return k.kohorte.landkreis.contains("Hamburg");
//				if (neu.kohorte.landkreis.contains("Heinsberg")) {
//			if (neu.kohorte.landkreis.contains("Tirschenreuth")) {
		return true;
	}

	private void printHeaders(List<Kohortenschritt> kohorten) {
		System.out.print("Datum");
		for (Kohortenschritt neu : kohorten) {
			if (includeKohorte(neu)) {
				System.out.print("\t" + neu.kohorte);
//				System.out.print("\tTote " + neu.kohorte);
			}
		}
		System.out.println();
	}

	private void printValues(List<Kohortenschritt> kohorten) {
		System.out.print(kohorten.get(0).meldedatum);
		for (Kohortenschritt neu : kohorten) {
			if (includeKohorte(neu)) {
//				System.out.print("\t" + (int) (neu.infected));
				System.out.print("\t" + (int) (100000 * neu.gesamtFaelle / neu.einwohner));
//				System.out.print("\t" + (neu.gesamtFaelle));
//				System.out.print("\t" + (neu.gesamtTode));
//				System.out.print("\t" + (int) (100 * neu.deltaSteigung));
//				System.out.print("\t" + (int) (100 * neu.newInfected / neu.infected));
			}
		}
		System.out.println();
	}

	private Kohorte createKohorte(RkiEntry rkiEntry) {
		return new Kohorte(rkiEntry.getBundesland());
//		return new Kohorte(rkiEntry.getLandkreis());
//		return new Kohorte(rkiEntry.getLandkreis(), rkiEntry.getAltersgruppe());
//		return new Kohorte("BRD", rkiEntry.getAltersgruppe());
	}

}
