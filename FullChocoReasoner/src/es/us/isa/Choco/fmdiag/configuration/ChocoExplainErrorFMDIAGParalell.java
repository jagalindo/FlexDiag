package es.us.isa.Choco.fmdiag.configuration;

import static choco.Choco.eq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
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

public class ChocoExplainErrorFMDIAGParalell extends ChocoQuestion implements ValidConfigurationErrorsQuestion {

	public boolean returnAllPossibeExplanations = false;
	private ChocoReasoner chReasoner;
	public List<String> explanations;

	public Map<String, Constraint> relations = null;
	public boolean flexactive = false;
	////////////For Parallel FlexDiag
	public int m = 1;

	Product s,r;
	public Map<String, Constraint> result = new HashMap<String, Constraint>();

	public int numberOfThreads = 4;
	ExecutorService executorService = Executors.newCachedThreadPool();
	
	public void setConfiguration(Product s) {
		this.s=s;
	}

	public void setRequirement(Product r) {
		this.r=r;
	}
	
	@Override
	public void setProduct(Product p) {
		this.s = p;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	public PerformanceResult answer(Reasoner r) throws FAMAException {
		chReasoner = (ChocoReasoner) r;
    	relations = new HashMap<String, Constraint>();

    	Map<String, Constraint> productConstraint = new HashMap<String, Constraint>();    	
		ArrayList<String> feats= new ArrayList<String>();
		
		for (GenericFeature f : this.s.getFeatures()) {
			IntegerVariable var = chReasoner.getVariables().get(f.getName());
			String name="U_" + f.getName();
			productConstraint.put(name, Choco.eq(var, 1));
			feats.add(name);
		}
		
		Map<String, Constraint> requirementConstraint = new HashMap<String, Constraint>();
		for (GenericFeature f : this.r.getFeatures()) {
			IntegerVariable var = chReasoner.getVariables().get(f.getName());
			requirementConstraint.put("R_" + f.getName(), Choco.eq(var, 1));
		}

		relations.putAll(chReasoner.getRelations());
		relations.putAll(requirementConstraint);
		relations.putAll(productConstraint);
		
		ArrayList<String> S = new ArrayList<String>(feats);
		ArrayList<String> AC = new ArrayList<String>(relations.keySet());

		if (returnAllPossibeExplanations == false) {
			List<String> fmdiag = fmdiag(S, AC);

			for (String s : fmdiag) {
				result.put(s, productConstraint.get(s));
			}

		} else {
			List<String> allExpl = new LinkedList<String>();
			List<String> fmdiag = fmdiag(S, AC);

			while (fmdiag.size() != 0) {
				allExpl.addAll(fmdiag);
				S.removeAll(fmdiag);
				AC.removeAll(fmdiag);
				fmdiag = fmdiag(S, AC);
			}
			for (String s : allExpl) {
				result.put(s, productConstraint.get(s));
			}
		}
		return new ChocoResult();
	}

	public List<String> fmdiag(List<String> S, List<String> AC) {
		//S is empty or (AC - S) non-consistent
		if (S.size() == 0 || !isConsistent(less(AC, S))) {
			return new ArrayList<String>();
		} 
		//(AC + S) is consistent
		else if (isConsistent(AC)){
			return new ArrayList<String>();
		}else { //(AC + S) is non-consistent
			diagThreads dt = new diagThreads(new ArrayList<String>(), S, AC, numberOfThreads, executorService);
			Future<List<String>> submit = executorService.submit(dt);
			
			try {
				return submit.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return new LinkedList<String>();
			} catch (ExecutionException e) {
				e.printStackTrace();
				return new LinkedList<String>();
			}
		}
	}
	
	public class diagThreads implements Callable<List<String>>{
		List<String> D, S, AC;
		int numberOfSplits;
		ExecutorService executorService;
		
		public diagThreads(List<String> D, List<String> S,List<String> AC,int numberOfSplits, ExecutorService executorService){
			this.D=D;
			this.S=S;
			this.AC=AC;
			this.executorService=executorService;
			this.numberOfSplits=numberOfSplits;		
		}
		
		/*Each thread (instance of this class) presents values for its attributes D, S, and AC. 
		 *(D + S) represents the set of rules to analyze; D and S are complementary, and S 
		 *corresponds to the current solution set.
		 *
		 *At the start point of the call() method , always:
		 *	- S represents the solution set.
		 *	- D represents the complement set of S concerning the previous solution set.
		 *	- AC represents the consistent rules of model C + the rules of S.  
		 *
		 *For the 1st thread always D is empty and S inconsistent. Then, AC is inconsistent.*/
		
		public List<String> call() throws Exception {
		/*1st base case*/
			if (D.size() != 0 && isConsistent(AC)){	
				/*Since AC does not contain D, when D is not empty and 
				 *AC is consistent, then possibly D contains inconsistencies*/ 
				List<String> nAC = plus(D, AC);
				
				if (!isConsistent(nAC)){			
					/*If (AC + D) contains inconsistencies, then D is analyzed to look for them*/
					diagThreads dt = new diagThreads(new ArrayList<String>(), D, nAC, numberOfSplits, executorService);
					Future<List<String>> submit = executorService.submit(dt);
					return submit.get();
				}
				else{ /*D is not the source of inconsistencies*/
					new ArrayList<String>();
				}
			}
			
		/*Since AC is non-consistent and D is not the inconsistencies source, then S is their source.
		 *If this solution is 'flexible' and the size of S is lesser or equal than m, then 
		 *S is the looked inconsistencies set (m defines the solution flexibility to 
		 *contains some consistent rules). 
		 *
		 *If this solution is not 'flexible' and S contains only one rule, then S is the looked inconsistent set*/
			
		/*2nd base case*/
			if(flexactive){
				if(S.size()<=m){
				   return S;
				}
			}else{				
				if(S.size()==1){
				   return S;
				}
			}
			
		/*outList corresponds to a results list for the threads of the solution*/
			List<List<String>> outLists= new LinkedList<List<String>>();
		
			////*DIVISION PHASE*////
			int div = 0; //div is the size of the partitions
						
			if (S.size() >= numberOfSplits){
			   div = S.size() / numberOfSplits;
			
			   if ((S.size() % numberOfSplits)>0)
				   div++;
			}
			else 
				div = 1;
			
			List<List<String>> splitListToSubLists = splitListToSubLists(S, div);

			
			////*CONQUER PHASE*////
			for(List<String> s: splitListToSubLists){
				/*For each partition 's', we define its complement 'rest' (AC - s) and  
				 *the rules set 'less' (AC - rest). 
				 *Then, a new thread 'dt' is defined with D=rest, S=s, and AC=less, 'dt' is run,
				 *and its results are grouped in the results list*/ 		
				List<String> rest= getRest(s,splitListToSubLists);	
				List<String> less = less(AC,rest);
				diagThreads dt = new diagThreads(rest, s,less , numberOfSplits, executorService);
				
				Future<List<String>> submit = executorService.submit(dt);
				outLists.add(submit.get());
			}
			
			/*We return the union of list of results*/
			return plus(outLists);
		}

		private List<String> getRest(List<String> s2, List<List<String>> splitListToSubLists) {
			LinkedList<String> res= new LinkedList<String>();
			for(List<String> c:splitListToSubLists){
				if(c!=s2){
					res.addAll(c);
				}
			}
			return res;
		}

		private List<String> plus(List<String> a1, List<String> a2) {
			List<String> res = new ArrayList<String>();
			res.addAll(a1);
			res.addAll(a2);
			return res;
		}

		private List<String> plus(List<List<String>> outLists) {
			List<String> res=new ArrayList<String>();
			for(List<String> s:outLists){	
				res.addAll(s);
			}
			return res;		
		}

		public <T> List<List<T>> splitListToSubLists(List<T> parentList, int subListSize) {
			  List<List<T>> subLists = new ArrayList<List<T>>();
			  
			  if (subListSize > parentList.size()) {
			     subLists.add(parentList);
			     } 
			  else {
			     int remainingElements = parentList.size();
			     int startIndex = 0;
			     int endIndex = subListSize;
			     do {
			        List<T> subList = parentList.subList(startIndex, endIndex);
			        subLists.add(subList);
			        startIndex = endIndex;
			        if (remainingElements - subListSize >= subListSize) {
			           endIndex = startIndex + subListSize;
			        } else {
			           endIndex = startIndex + remainingElements - subList.size();
			        }
			        remainingElements -= subList.size();
			     } while (remainingElements > 0);

			  }
			  return subLists;
		}
   }
	
	private List<String> less(List<String> aC, List<String> s2) {
		List<String> res = new ArrayList<String>();
		res.addAll(aC);
		res.removeAll(s2);
		return res;
	}

	private boolean isConsistent(Collection<String> aC) {
		Model p = new CPModel();
		p.addVariables(chReasoner.getVars());

		for (String rel : aC) {
			Constraint c = relations.get(rel);
			
			if (c == null) {
				System.out.println("Error");
			}
			p.addConstraint(c);
		}
		Solver s = new CPSolver();
		s.read(p);
		s.solve();
		return s.isFeasible();
	}	
}