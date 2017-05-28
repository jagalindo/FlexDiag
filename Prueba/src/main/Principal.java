package main;

import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAG;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;

public class Principal {

	public static void main(String[] args) throws WrongFormatException {

		String op = args[0];// flexdiag - prods - evolutionary

		if (op.equals("flexdiag")) {

			String modelPath = args[1];
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
			fmdiag.flexactive = true;
			fmdiag.m = m;

			reasoner.ask(fmdiag);

			System.out.println(modelPath.substring(modelPath.lastIndexOf('\\') + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf('\\') + 1) + "|" + m + "|" + fm.getFeaturesNumber()
					+ "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size() + "|"
					+ reasoner.getRelations().size() + "|" + fmdiag.result.keySet());

		}else if(op.equals("evolutionary")){

		}else if(op.equals("generateProducts")){
			
		}

	}

}
