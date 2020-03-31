package de.cheppner.corona.simulator.sim1;

import java.time.LocalDate;
import java.util.List;

public class Zeitschritt {
	LocalDate meldedatum;
	List<Kohortenschritt> kohorten;

	public Zeitschritt(LocalDate meldedatum, List<Kohortenschritt> kohorten) {
		super();
		this.meldedatum = meldedatum;
		this.kohorten = kohorten;
	}

}
