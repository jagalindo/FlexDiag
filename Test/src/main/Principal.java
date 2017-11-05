package main;

import java.io.File;

import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorEvolutionary;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAG;
import es.us.isa.Choco.fmdiag.model.ChocoExplainErrorFMDIAGParalell;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.questions.ChocoDetectErrorsQuestion;
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

			// ------------------------------

			XMLReader reader = new XMLReader();
			ProductManager pman = new ProductManager();

			FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
			
			/////////////////////*****
			ChocoReasoner reasoner = new ChocoReasoner();

			ChocoDetectErrorsQuestion Q1 = new ChocoDetectErrorsQuestion();
			Q1.setObservations(fm.getObservations());
			reasoner.ask(Q1);
			
			/////////////////////*****
			
//			Product prod = pman.readProduct(fm, productPath);			
			
			ChocoExplainErrorFMDIAGParalell flexdiagP = new ChocoExplainErrorFMDIAGParalell(m, t);
			flexdiagP.setErrors(Q1.getErrors());
			flexdiagP.returnAllPossibeExplanations=true;
			flexdiagP.flexactive = true;

			long start = System.currentTimeMillis();
			reasoner.ask(flexdiagP);
			long end = System.currentTimeMillis();
			
//			fmdiagP.setConfiguration(prod);
//			fmdiagP.setRequirement(new Product());
			
	//		fmdiagP.m = m;
			
/*
			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|"
					+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
					+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ fmdiagP.errors_explanations.keySet());
*/
			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + m + "|"
					+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
					+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ flexdiagP.errors_explanations.keySet());
		} 
		else if (op.equals("evolutionary")) {

			String productPath = args[2];
			XMLReader reader = new XMLReader();
			ProductManager pman = new ProductManager();

			FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
			Product prod = pman.readProduct(fm, productPath);

			ChocoReasoner reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);

			ChocoExplainErrorEvolutionary evol = new ChocoExplainErrorEvolutionary();
			evol.setConfiguration(prod);
			evol.setRequirement(new Product());
			long start = System.currentTimeMillis();

			reasoner.ask(evol);
			long end = System.currentTimeMillis();

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" +prod+ "|" + fm.getFeaturesNumber()
					+ "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size() + "|"
					+ reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ evol.result.keySet());

		} else if (op.equals("generateProducts")) {

			ProductManager man = new ProductManager();
			File dir = new File(modelPath);// directorio donde andan los xml
			for (File f : dir.listFiles()) {
				if (f.getName().endsWith(".xml")) {
					String newDirName = f.getName().replaceAll(".xml", "");
					XMLReader reader = new XMLReader();

					FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(f.getPath());
					int[] percentages = { 10, 20, 30, 40, 50, 100 };
					for (int p : percentages) {
						int psize = p * fm.getFeaturesNumber() / 100;
						File newDir = new File(dir.getPath() + File.separator + newDirName + File.separator);
						newDir.mkdirs();

						for (int i = 0; i < 10; i++) {
							Product generateProduct = man.generateProductUsingExcludes(fm, psize);
							for (int j = 0; j < 10; j++) {
								man.saveShuffledProduct(generateProduct,
										newDir.getPath() + File.separator +newDirName+"-"+ p + "-" + i + "-" + j+".prod");
								System.out.println("Generating and saving " + generateProduct);

							}
						}
					}

				}

			}
		} else if (op.equals("generateProductsSmall")) {

			ProductManager man = new ProductManager();
			File dir = new File(modelPath);// directorio donde andan los xml
			for (File f : dir.listFiles()) {
				if (f.getName().endsWith(".xml")) {
					String newDirName = f.getName().replaceAll(".xml", "");
					XMLReader reader = new XMLReader();

					FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(f.getPath());
					int[] percentages = { 10, 30, 50,100 };
					for (int p : percentages) {
						int psize = p * fm.getFeaturesNumber() / 100;
						File newDir = new File(dir.getPath() + File.separator + newDirName + File.separator);
						newDir.mkdirs();
						Product generateProduct = man.generateProductUsingExcludes(fm, psize);
						for (int j = 0; j < 10; j++) {
							man.saveShuffledProduct(generateProduct,
									newDir.getPath() + File.separator +newDirName+"-"+ p + "-" + j+".prod");
							System.out.println("Generating and saving " + generateProduct);

						}
					}

				}

			}
		} else if (op.equals("generateProductsCNF")) {

			ProductManager man = new ProductManager();
			File dir = new File(modelPath);// directorio donde andan los xml
			for (File f : dir.listFiles()) {
				if (f.getName().endsWith(".cnf")) {
					String newDirName = f.getName().replaceAll(".cnf", "");
					ChocoModel m = new ChocoModel();
					m.parseFile(f.getPath());
					int[] percentages = { 10, 20, 30, 40, 50 };
					for (int p : percentages) {
						int psize = p * m.getVariables().size() / 100;
						File newDir = new File(dir.getPath() + File.separator + newDirName + File.separator);
						newDir.mkdirs();

						for (int i = 0; i < 10; i++) {
							Product generateProduct = man.generateProductUsingExcludes(m, psize);
							for (int j = 0; j < 10; j++) {
								man.saveShuffledProduct(generateProduct,
										newDir.getPath() + File.separator +newDirName+"-"+ p + "-" + i + "-" + j+".prod");
								System.out.println("Generating and saving " + generateProduct);

							}
						}
					}

				}

			}
		} else if (op.equals("generateProductsCNFSmall")) {

			ProductManager man = new ProductManager();
			File dir = new File(modelPath);// directorio donde andan los xml
			for (File f : dir.listFiles()) {
				if (f.getName().endsWith(".cnf")) {
					String newDirName = f.getName().replaceAll(".cnf", "");
					ChocoModel m = new ChocoModel();
					m.parseFile(f.getPath());
					int[] percentages = { 10, 30, 50,100 };
					for (int p : percentages) {
						int psize = p * m.getVariables().size() / 100;
						File newDir = new File(dir.getPath() + File.separator + newDirName + File.separator);
						newDir.mkdirs();

						Product generateProduct = man.generateProductUsingExcludes(m, psize);
						for (int j = 0; j < 10; j++) {
							
							man.saveShuffledProduct(generateProduct,
									newDir.getPath() + File.separator +newDirName+"-"+ p + "-"  + j+".prod");
							System.out.println("Generating and saving " + generateProduct);

						}
					}

				}

			}
		} else if (op.equals("flexdiagpure")) {

			String productPath = args[2];
			Integer m = Integer.parseInt(args[3]);

			// ------------------------------

			ProductManager pman = new ProductManager();
			ChocoModel mod = new ChocoModel();
			mod.parseFile(modelPath);

			Product prod = pman.readProduct(mod, productPath);

			ChocoPureExplainErrorFMDIAG fmdiag = new ChocoPureExplainErrorFMDIAG();
			fmdiag.setConfiguration(prod);
			fmdiag.setRequirement(new Product());

			fmdiag.flexactive = true;
			fmdiag.m = m;
			long start = System.currentTimeMillis();
			fmdiag.answer(mod);
			long end = System.currentTimeMillis();

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + m + "|"
					+ mod.getVariables().size() + "|" + mod.getConstraints().size() + "|" +start+ "|"+ end+"|"+ fmdiag.result.keySet());

		} else if (op.equals("evolutionarypure")) {

			String productPath = args[2];
			ProductManager pman = new ProductManager();
			ChocoModel fm = new ChocoModel();
			fm.parseFile(modelPath);

			Product prod = pman.readProduct(fm, productPath);

			ChocoPureExplainErrorEvolutionary evol = new ChocoPureExplainErrorEvolutionary();
			evol.setConfiguration(prod);
			evol.setRequirement(new Product());

			evol.answer(fm);

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|"
					+ +fm.getVariables().size() + "|" + fm.getConstraints().size() + "|" + evol.result.keySet());

		}
	}

}
