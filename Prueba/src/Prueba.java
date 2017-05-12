import es.us.isa.Choco.fmdiag.ChocoExplainErrorEvolutionary;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.questions.ChocoDetectErrorsQuestion;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.variabilityModel.VariabilityModel;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

public class Prueba {

	public static void main(String[] args) throws WrongFormatException {
		// TODO Auto-generated method stub
		XMLReader reader = new XMLReader();
		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile("models/test.xml");
		
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
	
		//DetectErrors
		ChocoDetectErrorsQuestion deq = new ChocoDetectErrorsQuestion();
		deq.setObservations(fm.getObservations());
		reasoner.ask(deq);
		
		//Exec
		ChocoExplainErrorEvolutionary ee = new ChocoExplainErrorEvolutionary();
		ee.setErrors(deq.getErrors());
		reasoner.ask(ee);
		
		
	}

}
