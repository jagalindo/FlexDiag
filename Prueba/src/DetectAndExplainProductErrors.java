import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorEvolutionary;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAG;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

public class DetectAndExplainProductErrors {

	public static void main(String[] args) throws WrongFormatException {

		XMLReader reader = new XMLReader();
		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile("models/HIS.xml");
//		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile("models/test.xml");
		Product p = new Product();
		p.addFeature(new Feature("LIGHT-CONTROL"));//CAUSES ERROR
		p.addFeature(new Feature("POWER-LINE"));//CAUSES ERROR
		p.addFeature(new Feature("TEMPERATURE"));
		p.addFeature(new Feature("VIDEO"));
		p.addFeature(new Feature("LIGHT-CONTROL"));
		
		ChocoReasoner reasoner;
		
		//Evolution
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		
//		ChocoExplainErrorEvolutionary ee = new ChocoExplainErrorEvolutionary();
//		ee.setProduct(p);
//		reasoner.ask(ee);
//		
		System.out.println(ChocoExplainErrorEvolutionary.result);
		
		//FMDIAG 1
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
			
		ChocoExplainErrorFMDIAG fmdiag = new ChocoExplainErrorFMDIAG();
		fmdiag.setProduct(p);
		reasoner.ask(fmdiag);
		
		System.out.println(ChocoExplainErrorEvolutionary.result);

		
		//FMDIAG 2
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);

		ChocoExplainErrorFMDIAG fmdiag2 = new ChocoExplainErrorFMDIAG();
		fmdiag.setProduct(p);

		fmdiag2.returnAllPossibeExplanations=true;
		reasoner.ask(fmdiag);
	
		System.out.println(ChocoExplainErrorEvolutionary.result);
		
		
	}

}
