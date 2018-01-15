package main;

import java.io.File;

import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAG;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAGParalell2;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;

public class ParallelVSSecuential {

	public static void main(String[] args) throws WrongFormatException {
		
			String modelPath =  "./models/betty/model-100-10-1.xml";;
			String productPath = "./models/betty/model-100-10-1/model-100-10-1-10-0.prod";
			Integer m = 2;
			Integer t = 1;
			
			// ------------------------------
			// Secuential
			//------------------------------
			
			XMLReader reader = new XMLReader();
			ProductManager pman = new ProductManager();

			FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);

			Product prod = pman.readProduct(fm, productPath);

			ChocoReasoner reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);

			ChocoExplainErrorFMDIAG fmdiag = new ChocoExplainErrorFMDIAG();
    		fmdiag.setConfiguration(prod);
			fmdiag.setRequirement(new Product());
			fmdiag.flexactive = true;
			fmdiag.m = m;
			long start = System.currentTimeMillis();
			reasoner.ask(fmdiag);
			long end = System.currentTimeMillis();

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|"
					+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
					+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ fmdiag.result.keySet());

			
			//------------------------------
			// Parallel
			//------------------------------
			

			reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);

			ChocoExplainErrorFMDIAGParalell2 flexdiagP  = new ChocoExplainErrorFMDIAGParalell2(m, t);
			flexdiagP.setConfiguration(prod);
			flexdiagP.setRequirement(new Product());
			flexdiagP.flexactive = true;
	
			start = System.currentTimeMillis();
			reasoner.ask(flexdiagP);
			end = System.currentTimeMillis();

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|" + t + "|"
					+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
					+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ flexdiagP.result.keySet());
		
	}

}
