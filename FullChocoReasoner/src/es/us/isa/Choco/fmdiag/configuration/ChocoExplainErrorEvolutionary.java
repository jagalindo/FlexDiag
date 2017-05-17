package es.us.isa.Choco.fmdiag.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jenetics.BitChromosome;
import org.jenetics.BitGene;
import org.jenetics.Genotype;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.util.Factory;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.Solver;
import es.us.isa.ChocoReasoner.ChocoQuestion;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.ChocoResult;
import es.us.isa.FAMA.Benchmarking.PerformanceResult;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.Reasoner.Reasoner;
import es.us.isa.FAMA.Reasoner.questions.ValidConfigurationErrorsQuestion;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;

public class ChocoExplainErrorEvolutionary extends ChocoQuestion implements ValidConfigurationErrorsQuestion {

	public boolean returnAllPossibeExplanations = false;
	private static ChocoReasoner chReasoner;

	static Map<String, Constraint> relations = null;

	public static Map<String, Constraint> result = new HashMap<String, Constraint>();
	static Map<String, Constraint> productConstraint= new HashMap<String, Constraint>();
	static ArrayList<Constraint> cons = new ArrayList<Constraint>();
	static ArrayList<String> consStr = new ArrayList<String>();
	Product p;

	@Override
	public void setProduct(Product p) {
		this.p = p;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	public static Integer eval(Genotype<BitGene> gt) {
		int res = Integer.MAX_VALUE;

		try {
			int i = 0;

			Model p = new CPModel();
			p.addVariables(chReasoner.getVars());
			
			//Add model constraints
			for(Constraint c: relations.values()){
				p.addConstraints(c);	
			}
			
//			for(Constraint c: productConstraint.values()){
//				p.addConstraints(c);	
//			}
			
			//Add product constraints
			Iterator<BitGene> iterator = gt.getChromosome().iterator();
			while (iterator.hasNext()) {
				if (!iterator.next().booleanValue()) {
					p.addConstraint(cons.get(i));
				}
				i++;
			}
			
			Solver s = new CPSolver();
			s.read(p);
			s.solve();
			
			if (!s.isFeasible()) {
				res = gt.getChromosome().as(BitChromosome.class).bitCount();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}

		return res;
	}

	public PerformanceResult answer(Reasoner r) throws FAMAException {

		ChocoResult res = new ChocoResult();
		chReasoner = (ChocoReasoner) r;



		Map<String, Constraint> productConstraint = new HashMap<String, Constraint>();

		for (GenericFeature f : p.getFeatures()) {
			Constraint eq2 = Choco.eq(chReasoner.getVariables().get(f), 1);
			String n="U_" + f.getName();
			productConstraint.put(n,eq2);
			cons.add(eq2);
			consStr.add(n);
		}

		
		// 1.) Define the genotype (factory) suitable
		// for the problem.
		Factory<Genotype<BitGene>> gtf = Genotype.of(BitChromosome.of(productConstraint.size(), 0.3));

		// 3.) Create the execution environment.
		Engine<BitGene, Integer> engine = Engine.builder(ChocoExplainErrorEvolutionary::eval, gtf)
				.executor((Executor) Runnable::run).minimizing().build();

		// 4.) Start the execution (evolution) and collect the result.
		Genotype<BitGene> result = engine.stream().limit(100).collect(EvolutionResult.toBestGenotype());

		for (int g = 0; g < result.getChromosome().length(); g++) {
			if (!result.getChromosome().getGene(g).booleanValue()) {
				String C = consStr.get(g);
				ChocoExplainErrorEvolutionary.result.put(C,productConstraint.get(C));
			}
		}

		return res;

	}

	

}
