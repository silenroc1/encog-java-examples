package org.encog.examples.neural.benchmark;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationReLU;
import org.encog.examples.proben.BenchmarkDefinition;
import org.encog.examples.proben.ProBenData;
import org.encog.examples.proben.ProBenResultAccumulator;
import org.encog.examples.proben.ProBenRunner;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.MLMethod;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.RequiredImprovementStrategy;
import org.encog.ml.train.strategy.end.EndIterationsStrategy;
import org.encog.ml.train.strategy.end.SimpleEarlyStoppingStrategy;
import org.encog.neural.error.ATanErrorFunction;
import org.encog.neural.error.CrossEntropyErrorFunction;
import org.encog.neural.error.ErrorFunction;
import org.encog.neural.error.LinearErrorFunction;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.ContainsFlat;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

public class BenchmarkErrorFunctions implements BenchmarkDefinition {
	
	private String probenFolder;
	private ErrorFunction errorFn;
	
	BenchmarkErrorFunctions(String theProbenFolder, ErrorFunction theErrorFn) {
		this.probenFolder = theProbenFolder;
		this.errorFn = theErrorFn;
	}
	
	public MLMethod createMethod(ProBenData data) {
		int hiddenCount = (int)((data.getInputCount()+data.getIdealCount())*0.5);
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,true,data.getInputCount()));
		network.addLayer(new BasicLayer(new ActivationReLU(),true,hiddenCount));
		network.addLayer(new BasicLayer(new ActivationLinear(),false,data.getIdealCount()));
		network.getStructure().finalizeStructure();
		network.reset();
		(new RangeRandomizer(-10,10)).randomize(network);
		return network;
	}
	
	public MLTrain createTrainer(MLMethod method, ProBenData data) {
		final ResilientPropagation train = new ResilientPropagation(
				(ContainsFlat)method, data.getTrainingDataSet());
		train.setErrorFunction(this.errorFn);
		train.addStrategy(new SimpleEarlyStoppingStrategy(data.getValidationDataSet(),50));
		train.addStrategy(new RequiredImprovementStrategy(100));
		train.addStrategy(new EndIterationsStrategy(2000));
		return train;
	}
	
	public String getProBenFolder() {
		return this.probenFolder;
	}
	
	public static ProBenResultAccumulator benchmarkLinear(String probenPath) {
		System.out.println("Starting Linear...");
		BenchmarkErrorFunctions def = new BenchmarkErrorFunctions(probenPath, new LinearErrorFunction());
		ProBenRunner runner = new ProBenRunner(def);
		return runner.run();
	}
	
	public static ProBenResultAccumulator benchmarkArctan(String probenPath) {
		System.out.println("Starting Arctan...");
		BenchmarkErrorFunctions def = new BenchmarkErrorFunctions(probenPath, new ATanErrorFunction());
		ProBenRunner runner = new ProBenRunner(def);
		return runner.run();
	}
	
	public static ProBenResultAccumulator benchmarkCrossEntropy(String probenPath) {
		System.out.println("Starting CrossEntropy...");
		BenchmarkErrorFunctions def = new BenchmarkErrorFunctions(probenPath, new CrossEntropyErrorFunction());
		ProBenRunner runner = new ProBenRunner(def);
		return runner.run();
	}
	
	public static void main(String[] args) {		
		String probenPath = ProBenData.obtainProbenPath(args);
		
		System.out.println("Starting...");
		ProBenResultAccumulator linear = benchmarkLinear(probenPath);
		ProBenResultAccumulator arctan = benchmarkArctan(probenPath);
		ProBenResultAccumulator crossEntropy = benchmarkCrossEntropy(probenPath);

		System.out.println("Linear: " + linear.toString());
		System.out.println("Arctan: " + arctan.toString());
		System.out.println("Cross Entropy: " + crossEntropy.toString());

		Encog.getInstance().shutdown();

		
	}

	@Override
	public boolean shouldCenter() {
		return true;
	}

	@Override
	public double getInputCenter() {
		return 0;
	}

	@Override
	public double getOutputCenter() {
		return 1;
	}
}
