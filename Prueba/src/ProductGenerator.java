import java.io.File;

import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;

public class ProductGenerator {

		
	public static void main(String[] args) throws WrongFormatException {
		ProductManager man = new ProductManager();
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
						Product generateProduct = man.generateProduct(fm, psize);
						
						man.saveProduct(generateProduct, newDir.getPath()+"\\"+p+"-"+i);
					}
				}
				
       
	        }

		}
		
	}
	

}
