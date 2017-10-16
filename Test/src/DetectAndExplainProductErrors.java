import java.util.Collections;
import java.util.List;

import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAG;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;

public class DetectAndExplainProductErrors {

	public static void main(String[] args) throws WrongFormatException {

		XMLReader reader = new XMLReader();
		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile("input/tiny/tiny.xml");
		ProductManager helper = new ProductManager();
		int psize = 10 * fm.getFeaturesNumber() / 100;
		Product product = helper.generateProductUsingExcludes(fm, psize);
		

		Product r = new Product();
		

		for (int i=0;i<10;i++) {
			System.out.println("Executing Product: "+product);
			ChocoReasoner reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);
			ChocoExplainErrorFMDIAG fmdiag = new ChocoExplainErrorFMDIAG();
			fmdiag.setConfiguration(product);
			fmdiag.setRequirement(r);
			reasoner.ask(fmdiag);

			System.out.println("Result: "+fmdiag.result.keySet());
			System.out.println("-------------");
			Collections.shuffle((List<?>) product.getFeatures());

		}

	}

}
