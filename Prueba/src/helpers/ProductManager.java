package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.questions.ChocoValidProductQuestion;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.featureModel.Product;

public class ProductManager {

	public Product generateProduct(FAMAFeatureModel fm, Integer inSize) {
		// Verificamos
		Integer size = inSize;
		if (size > fm.getFeaturesNumber()) {
			size = fm.getFeaturesNumber();
		}

		Product res = null;
		int tries = 0;
		while (res == null) {
			tries++;
			List<Feature> feats = (List<Feature>) fm.getFeatures();
			Collections.shuffle(feats);
			Product temp = new Product();
			for (Feature f : feats.subList(0, size)) {
				temp.addFeature(f);
			}
			ChocoReasoner reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);
			ChocoValidProductQuestion vqp = new ChocoValidProductQuestion();
			vqp.setProduct(temp);
			reasoner.ask(vqp);
			if (!vqp.isValid()) {
				res = temp;
			}
			if (tries > 9) {
				res = new Product();
				System.err.println("Number of tries exceeded");
			}

		}
		return res;
	}

	public Product generateProduct(ChocoModel fm, Integer inSize) {
		Integer size = inSize;
		if (size > fm.variables.size()) {
			size = fm.variables.size();
		}

		Product res = null;
		int tries = 0;
		while (res == null) {
			tries++;
			List<Integer> tmpProd = new LinkedList<>(fm.variables.keySet());
			Collections.shuffle(tmpProd);
			tmpProd = tmpProd.subList(0, size);

			if (!fm.isValidProduct(tmpProd)) {
				System.out.println("ping");
				res = new Product();
				for (Integer i : tmpProd) {
					res.addFeature(new Feature(i + ""));
				}
			}
			if (tries > 10) {
				res = new Product();
				System.err.println("Number of tries exceeded");
			}

		}
		return res;
	}

	public void saveProduct(Product p, String path) {
		try (PrintWriter out = new PrintWriter(path)) {
			out.println(p.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void saveShuffledProduct(Product p, String path) {
		try (PrintWriter out = new PrintWriter(path)) {
			Collections.shuffle((List<?>) p.getFeatures());
			out.println(p.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void saveProducts(Collection<Product> prods, String path) {
		try (PrintWriter out = new PrintWriter(path)) {
			for (Product p : prods) {
				out.println(p.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public Collection<Product> readProducts(FAMAFeatureModel fm, String path) {
		Collection<Product> res = null;
		try {
			res = new LinkedList<Product>();
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				Product p = new Product();
				StringTokenizer tokenizer = new StringTokenizer(line, ";");
				while (tokenizer.hasMoreTokens()) {
					Feature feature = fm.searchFeatureByName(tokenizer.nextToken());
					p.addFeature(feature);
				}
				res.add(p);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public Product readProduct(FAMAFeatureModel fm, String path) {
		List<Product> prods = (List<Product>) readProducts(fm, path);
		return prods.get(0);
	}

	public Collection<Product> readProducts(ChocoModel fm, String path) {
		Collection<Product> res = null;
		try {
			res = new LinkedList<Product>();
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				Product p = new Product();
				StringTokenizer tokenizer = new StringTokenizer(line, ";");
				while (tokenizer.hasMoreTokens()) {
					Feature feature = new Feature(tokenizer.nextToken());
					p.addFeature(feature);
				}
				res.add(p);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public Product readProduct(ChocoModel fm, String path) {
		List<Product> prods = (List<Product>) readProducts(fm, path);
		return prods.get(0);
	}
}
