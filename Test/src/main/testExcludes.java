package main;

import java.io.File;
import java.util.Iterator;

import es.us.isa.FAMA.models.FAMAfeatureModel.Dependency;
import es.us.isa.FAMA.models.FAMAfeatureModel.ExcludesDependency;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

public class testExcludes {

	public static void main(String[] args) throws WrongFormatException {

		File dir = new File("./input/debian");
		for (File model : dir.listFiles()) {
			XMLReader reader = new XMLReader();
			FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(model.getAbsolutePath());
			int e = countExcludes(fm);
			if (e > 0) {
				System.out.println(model.getName() + ";" + e);
			}else {
				System.out.println("NO EXCLUDES!!");
			}
		}
	}

	public static int countExcludes(FAMAFeatureModel fm) {
		int res = 0;
		Iterator<Dependency> dependencies = fm.getDependencies();
		while (dependencies.hasNext()) {
			Dependency next = dependencies.next();
			if (next instanceof ExcludesDependency) {
				res++;
			}
		}
		return res;
	}
}
