import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileNameExtensionFilter;

import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.questions.ChocoValidProductQuestion;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

public class ProductGenerator {

	public static Product generateProduct(FAMAFeatureModel fm, Integer inSize) {
		// Verificamos 
		Integer size = inSize;
		if(size>fm.getFeaturesNumber()){
			size=fm.getFeaturesNumber();
		}
		
		Product res = null;
		int tries=0;
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
			if(tries>9){
				res=new Product();
				System.err.println("Number of tries exceeded");
			}

		}
		return res;
	}

	public static void saveProduct(Product p, String path) {
		try (PrintWriter out = new PrintWriter(path)) {
			out.println(p.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void saveProducts(Collection<Product> prods, String path) {
		try (PrintWriter out = new PrintWriter(path)) {
			for(Product p : prods){
				out.println(p.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static Collection<Product> readProducts(FAMAFeatureModel fm, String path){
			Collection<Product> res =null;
			try {
				res = new LinkedList<Product>();
				File file = new File(path);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					Product p = new Product();
					StringTokenizer tokenizer = new StringTokenizer(line, ";");
					while(tokenizer.hasMoreTokens()){
						Feature feature= fm.searchFeatureByName(tokenizer.nextToken());
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

	public static Product readProduct(FAMAFeatureModel fm, String path){
			List<Product> prods = (List<Product>) readProducts(fm, path);
			return prods.get(0);
	}
	
	public static void main(String[] args) throws WrongFormatException {
		File dir = new File("./models/splot-23-5-17/");
		for(File f: dir.listFiles()){
			if (f.getName().endsWith(".xml")) {
				String newDirName = f.getName().replaceAll(".xml", "");
				XMLReader reader = new XMLReader();
				
				FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(f.getPath());
				int[] percentages = {10,20,30,40,50};
				for(int p:percentages){
					int psize=p*fm.getFeaturesNumber()/100;
					File newDir = new File(dir.getPath()+"\\"+newDirName+"\\");
					newDir.mkdirs();
					
					for(int i =0;i<10;i++){
						Product generateProduct = generateProduct(fm, psize);
						
						saveProduct(generateProduct, newDir.getPath()+"\\"+p+"-"+i);
					}
				}
				
       
	        }

		}
		
	}
	

}
