package es.us.isa.Choco.fmdiag;

import static choco.Choco.eq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.jenetics.BitChromosome;
import org.jenetics.BitGene;
import org.jenetics.Genotype;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.util.Factory;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import es.us.isa.ChocoReasoner.ChocoQuestion;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.ChocoResult;
import es.us.isa.FAMA.Benchmarking.PerformanceResult;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.Reasoner.Reasoner;
import es.us.isa.FAMA.Reasoner.questions.ExplainErrorsQuestion;
import es.us.isa.FAMA.errors.Error;
import es.us.isa.FAMA.errors.Explanation;
import es.us.isa.FAMA.errors.Observation;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.GenericRelation;
import es.us.isa.FAMA.models.variabilityModel.VariabilityElement;

public class ChocoExplainErrorEvolutionary extends ChocoQuestion implements ExplainErrorsQuestion {

	public boolean returnAllPossibeExplanations = false;
	private static ChocoReasoner chReasoner;
	public List<String> explanations;

	Collection<Error> errors;
	Map<String, Constraint> relations = null;
	static ArrayList<Constraint> cons = new ArrayList<Constraint>();
	static ArrayList<String> consStr = new ArrayList<String>();

	public boolean flexactive = false;
	public int m = 1;



	public static Integer eval(Genotype<BitGene> gt) {
		int res= Integer.MAX_VALUE;

		try{
			int i=0;

			Model p = new CPModel();
			p.addVariables(chReasoner.getVars());

			Iterator<BitGene> iterator = gt.getChromosome().iterator();
			while(iterator.hasNext()){
				if(iterator.next().booleanValue()){
					p.addConstraint(cons.get(i));
				}
				i++;
			}
			Solver s = new CPSolver();
			s.read(p);
			s.solve();
			if(!s.isFeasible()){
				res = gt.getChromosome().as(BitChromosome.class).bitCount();
			}	
		}catch(ArrayIndexOutOfBoundsException e){}
				
		//System.out.println(res);
		return res;
	}
	
	public PerformanceResult answer(Reasoner r) throws FAMAException {

		ChocoResult res = new ChocoResult();
		chReasoner = (ChocoReasoner) r;

		if ((errors == null) || errors.isEmpty()) {
			errors = new LinkedList<Error>();
			return res;
		}

		Iterator<Error> itE = this.errors.iterator();
		Map<String, IntegerVariable> vars = chReasoner.getVariables();
		Map<String, IntegerExpressionVariable> setVars = chReasoner.getSetRelations();
		// mientras haya errores
		while (itE.hasNext()) {
			// crear una lista de constraints, que impondremos segun las
			// observaciones
			Error e = itE.next();

			// System.out.println("Explanations for "+e.toString());
			Map<String, Constraint> cons4obs = new HashMap<String, Constraint>();
			Observation obs = e.getObservation();
			Map<? extends VariabilityElement, Object> values = obs.getObservation();
			Iterator<?> its = values.entrySet().iterator();

			// mientras haya observations
			// las imponemos al problema como restricciones
			while (its.hasNext()) {
				int i = 0;
				try {
					Entry<? extends VariabilityElement, Object> entry = (Entry<? extends VariabilityElement, Object>) its.next();
					Constraint cn;
					int value = (Integer) entry.getValue();
					VariabilityElement ve = entry.getKey();
					if (ve instanceof GenericFeature) {
						IntegerVariable arg0 = vars.get(ve.getName());
						cn = eq(arg0, value);
					} else {
						IntegerExpressionVariable arg0 = setVars.get(ve.getName());
						cn = eq(arg0, value);
					}
					cons4obs.put("Temporary" + i, cn);
					i++;
				} catch (ClassCastException exc) {
				}
			}

			// solve the problem y fmdiag
			relations = new HashMap<String, Constraint>();
			relations.putAll(cons4obs);
			relations.putAll(chReasoner.getRelations());
			
			for(Entry<String,Constraint> c: relations.entrySet()){
				consStr.add(c.getKey());
				cons.add(c.getValue());
			}

			
			// 1.) Define the genotype (factory) suitable
			// for the problem.
			Factory<Genotype<BitGene>> gtf = Genotype.of(BitChromosome.of(relations.size(), 0.3));

			// 3.) Create the execution environment.
			Engine<BitGene, Integer> engine = Engine.builder(ChocoExplainErrorEvolutionary::eval, gtf).executor( (Executor) Runnable :: run).minimizing().build();

			// 4.) Start the execution (evolution) and collect the result.
			Genotype<BitGene> result = engine.stream().limit(100).collect(EvolutionResult.toBestGenotype());
			
			for(int g =0;g<result.getChromosome().length();g++){
				if(result.getChromosome().getGene(g).booleanValue()){
					GenericRelation relation = chReasoner.relation.get(consStr.get(g));
					
					if(relation !=null){
						Explanation exp = new Explanation();

					exp.addRelation(relation);
					e.addExplanation(exp);

					}
					//System.err.println("Not yet finished");
				}
			}
//			System.out.println(result.getChromosome().as(BitChromosome.class).bitCount());
			// queda por mirar que no cuente el numero de ctc pero el de constraints con posibilidad de tener el error
		}

		return new ChocoResult();

	}





	public void setErrors(Collection<Error> colErrors) {
		this.errors = colErrors;
	}

	public Collection<Error> getErrors() {
		return errors;
	}

}
