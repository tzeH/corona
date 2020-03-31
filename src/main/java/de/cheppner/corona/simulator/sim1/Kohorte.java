package de.cheppner.corona.simulator.sim1;

import java.util.Comparator;

public class Kohorte implements Comparable<Kohorte> {

	String landkreis;
	String altersgruppe;

	public Kohorte(String landkreis) {
		super();
		this.landkreis = landkreis;
	}

	public Kohorte(String landkreis, String altersgruppe) {
		super();
		this.landkreis = landkreis;
		this.altersgruppe = altersgruppe;
	}

	public String getLandkreis() {
		return landkreis;
	}

	public String getAltersgruppe() {
		return altersgruppe;
	}

	@Override
	public String toString() {
		if (landkreis == null) {
			return altersgruppe;
		}
		if (altersgruppe == null) {
			return landkreis;
		}
		return landkreis + "-" + altersgruppe;
	}

	@Override
	public int compareTo(Kohorte o) {
		if (altersgruppe != null) {
			int c = Comparator.comparing(Kohorte::getAltersgruppe).compare(this, o);
			if (c != 0) {
				return c;
			}
		}
		return Comparator.comparing(Kohorte::getLandkreis).compare(this, o);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((altersgruppe == null) ? 0 : altersgruppe.hashCode());
		result = prime * result + ((landkreis == null) ? 0 : landkreis.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Kohorte other = (Kohorte) obj;
		if (altersgruppe == null) {
			if (other.altersgruppe != null)
				return false;
		} else if (!altersgruppe.equals(other.altersgruppe))
			return false;
		if (landkreis == null) {
			if (other.landkreis != null)
				return false;
		} else if (!landkreis.equals(other.landkreis))
			return false;
		return true;
	}

}
