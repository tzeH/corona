package de.cheppner.corona.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import de.cheppner.corona.simulator.sim1.Simulator1;

public class Startup {

	public static void main(String[] args) throws IOException, SchemaException {
		List<RkiEntry> rkiDaten = parseRki();

		Map<String, Landkreis> landkreise = parseLandkreise2();

		new Simulator1(rkiDaten, landkreise).run();

		if (false) {
			int found = 0;
			for (RkiEntry rkiEntry : rkiDaten) {
				String landkreis = rkiEntry.getLandkreis();
				Landkreis feature = landkreise.get(landkreis);
				if (feature == null) {
					System.out.println("Kein Feature fuer " + rkiEntry);
				} else {
					found++;
				}

				System.out.println("Gefunden: " + found + " / " + rkiDaten.size());
			}
		}
	}

	private static Map<String, Landkreis> parseLandkreise2()
			throws FileNotFoundException, IOException, SchemaException {
		File file = new File("data/RKI_Corona_Landkreise.shp");
		Map<String, Object> map = new HashMap<>();
		map.put("url", file.toURI().toURL());
		map.put("charset", Charset.forName("UTF-8"));

		DataStore dataStore = DataStoreFinder.getDataStore(map);
		String typeName = dataStore.getTypeNames()[0];

		FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
		Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

		Map<String, Landkreis> landkreise = new HashMap<>();
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
		try (FeatureIterator<SimpleFeature> features = collection.features()) {
			while (features.hasNext()) {
				SimpleFeature feature = features.next();
//				Collection<Property> attributes = feature.getProperties();
//				for (Property object : attributes) {
//					if (!object.getName().toString().equals("the_geom"))
//						System.out.println(object.getName() + " " + object.getValue());
//				}

				Landkreis landkreis = Landkreis.parseRkiShapeFile(feature);
				landkreise.put(landkreis.getName(), landkreis);
			}
		}
		return landkreise;
	}

	private static List<RkiEntry> parseRki() throws IOException {
		try (ZipInputStream inputStream = new ZipInputStream(new FileInputStream("data/RKI_COVID19.zip"))) {
			inputStream.getNextEntry();

			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
				return bufferedReader.lines().skip(1).map(RkiEntry::fromCsv).collect(Collectors.toList());
			}
		}

	}

}
