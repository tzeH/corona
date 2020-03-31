package de.cheppner.corona.simulator;

import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;

public class Landkreis {
	private MultiPolygon geometry;
	private int Einwohnerzahl;
	private String name, bundesland;

	public static Landkreis parseRkiShapeFile(SimpleFeature feature) {
		Landkreis result = new Landkreis();
		result.geometry = (MultiPolygon) feature.getDefaultGeometry();
		result.name = (String) feature.getAttribute("county");
		result.bundesland = (String) feature.getAttribute("BL");
		result.Einwohnerzahl = ((Integer) feature.getAttribute("EWZ")).intValue();
		return result;
	}

	@Override
	public String toString() {
		return "Landkreis [Einwohnerzahl=" + Einwohnerzahl + ", name=" + name + "]";
	}

	public MultiPolygon getGeometry() {
		return geometry;
	}

	public String getBundesland() {
		return bundesland;
	}

	public int getEinwohnerzahl() {
		return Einwohnerzahl;
	}

	public String getName() {
		return name;
	}

}
