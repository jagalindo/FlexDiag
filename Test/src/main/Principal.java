package main;

import java.io.File;
import java.util.List;
import java.util.Set;

import choco.kernel.model.constraints.Constraint;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorEvolutionary;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAG;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAGParalell2;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ChocoModel;
import helpers.ChocoPureExplainErrorEvolutionary;
import helpers.ChocoPureExplainErrorFMDIAG;
import helpers.ProductManager;

public class Principal {

	public static void main(String[] args) throws WrongFormatException {

		String op = args[0];// flexdiag - prods - evolutionary
		String modelPath = args[1];

		if (op.equals("flexdiag")) {
			String productPath = args[2];
			Integer m = Integer.parseInt(args[3]);

			// ------------------------------

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

		}else if (op.equals("flexdiagP")) {
			String productPath = args[2];
			Integer m = Integer.parseInt(args[3]);
			Integer t = Integer.parseInt(args[4]);
			
			//------------------------------

			XMLReader reader = new XMLReader();
			ProductManager pman = new ProductManager();

			FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);

			Product prod = pman.readProduct(fm, productPath);

			ChocoReasoner reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);

			ChocoExplainErrorFMDIAGParalell2 flexdiagP  = new ChocoExplainErrorFMDIAGParalell2(m, t);
			flexdiagP.setConfiguration(prod);
			flexdiagP.setRequirement(new Product());
			flexdiagP.flexactive = true;
	
			long start = System.currentTimeMillis();
			reasoner.ask(flexdiagP);
			long end = System.currentTimeMillis();

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|" + t + "|"
					+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
					+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ flexdiagP.result.keySet());
		} 
	}

}
