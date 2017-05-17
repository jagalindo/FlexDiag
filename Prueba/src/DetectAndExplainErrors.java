import es.us.isa.Choco.fmdiag.model.ChocoExplainErrorEvolutionary;
import es.us.isa.Choco.fmdiag.model.ChocoExplainErrorFMDIAG;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.questions.ChocoDetectErrorsQuestion;
import es.us.isa.ChocoReasoner4Exp.questions.ChocoExplainErrorsQuestion;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.variabilityModel.VariabilityModel;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

public class DetectAndExplainErrors {

	public static void main(String[] args) throws WrongFormatException {
		// TODO Auto-generated method stub
		XMLReader reader = new XMLReader();
		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile("models/ErrorsExample.xml");
//		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile("models/test.xml");

		ChocoDetectErrorsQuestion deq;
		ChocoReasoner reasoner;
		
		//Evolution
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		deq = new ChocoDetectErrorsQuestion();
		deq.setObservations(fm.getObservations());
		reasoner.ask(deq);

		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		ChocoExplainErrorEvolutionary ee = new ChocoExplainErrorEvolutionary();
		ee.setErrors(deq.getErrors());
		reasoner.ask(ee);
		for(es.us.isa.FAMA.errors.Error e:ee.getErrors()){
			System.out.println(e);
			System.out.println("ErrorEvol: "+e.getExplanations().size());
			System.out.println(e.getExplanations());

		}
		
		//FMDIAG 1
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		deq = new ChocoDetectErrorsQuestion();
		deq.setObservations(fm.getObservations());
		reasoner.ask(deq);
		
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		ChocoExplainErrorFMDIAG fmdiag = new ChocoExplainErrorFMDIAG();
		fmdiag.setErrors(deq.getErrors());
		reasoner.ask(fmdiag);
		for(es.us.isa.FAMA.errors.Error e:fmdiag.getErrors()){
			System.out.println(e);
			System.out.println("ErrorFMDIAG: "+e.getExplanations().size());
			System.out.println(e.getExplanations());

		}
		
		//FMDIAG 2
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		deq = new ChocoDetectErrorsQuestion();
		deq.setObservations(fm.getObservations());
		reasoner.ask(deq);
		
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		ChocoExplainErrorFMDIAG fmdiag2 = new ChocoExplainErrorFMDIAG();
		fmdiag2.setErrors(deq.getErrors());
		fmdiag2.returnAllPossibeExplanations=true;
		reasoner.ask(fmdiag2);
		for(es.us.isa.FAMA.errors.Error e:fmdiag2.getErrors()){			
			System.out.println(e);
			System.out.println("ErrorFMDIAG: "+e.getExplanations().size());
			System.out.println(e.getExplanations());

		}
		
		//REITERS
		reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		deq = new ChocoDetectErrorsQuestion();
		deq.setObservations(fm.getObservations());
		reasoner.ask(deq);
		es.us.isa.ChocoReasoner4Exp.ChocoReasoner chExp = new es.us.isa.ChocoReasoner4Exp.ChocoReasoner();
		fm.transformTo(chExp);
		ChocoExplainErrorsQuestion ceeq= new ChocoExplainErrorsQuestion();
		ceeq.setErrors(deq.getErrors());
		chExp.ask(ceeq);

		for(es.us.isa.FAMA.errors.Error e:ceeq.getErrors()){
			System.out.println(e);
			System.out.println("ErrorReiter: "+e.getExplanations().size());
			System.out.println(e.getExplanations());
		}
	}

}
