package de.cheppner.corona.simulator.sim1;

import java.time.LocalDate;

class Kohortenschritt {
	LocalDate meldedatum;
	Kohorte kohorte;
	int neueFaelle;
	int neueTode;
	int gesamtFaelle;
	int gesamtTode;
	double deltaSteigung;
	double suspectible;
	double infected;
	double newInfected;
	double removed;
	double einwohner;

	public Kohortenschritt(LocalDate meldedatum, Kohorte kohorte, int neueFaelle, int neueTode) {
		this.meldedatum = meldedatum;
		this.kohorte = kohorte;
		this.neueFaelle = neueFaelle;
		this.neueTode = neueTode;
	}

	public Kohorte getKohorte() {
		return kohorte;
	}

	@Override
	public String toString() {
		return "Kohortenschritt [meldedatum=" + meldedatum + ", kohorte=" + kohorte + ", neueFaelle=" + neueFaelle
				+ ", neueTode=" + neueTode + ", gesamtFaelle=" + gesamtFaelle + ", gesamtTode=" + gesamtTode
				+ ", einwohner=" + einwohner + "]";
	}

}
