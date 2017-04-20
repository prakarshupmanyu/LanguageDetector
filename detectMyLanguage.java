import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

public class detectMyLanguage {

	private static final int n = 3; // value of n in n-gram
	private static final String desktopPath = "/home/prakarsh/Desktop/";	//path to desktop
	
	private static final String training_directory = desktopPath + "Training Set";
	private static final String test_directory = desktopPath + "Test Set "+ n + " grams";
	private static final String languages[] = { "Bulgarian", "Czech", "Danish",
			"English", "French", "German", "Greek", "Italian", "Polish",
			"Spanish" };
	private static final double maxDouble = Double.MAX_VALUE;
	
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Do you wish to train your model first (y/n)?");
		try{
			String input = br.readLine();
			while(!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))){
				System.out.println("Please enter your choice as y or n : ");
				System.out.println("Do you wish to train your model first (y/n)?");
				input = br.readLine();
			}
			char choice = input.charAt(0);
			if(choice == 'y'){
				detectMyLanguage.preprocessTrainingData();
			}
			else{
				detectMyLanguage.identifyLanguage();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static HashMap<String, HashMap<String, Float>> getLanguageModel() {

		HashMap<String, HashMap<String, Float>> languageModel = new HashMap<String, HashMap<String, Float>>();

		for (int langCount = 0; langCount < languages.length; langCount++) {

			String filename_read = test_directory + "/" + languages[langCount] + "_result.txt";
			HashMap<String, Float> map = new HashMap<String, Float>();

			try {
				// read from training data
				BufferedReader reader = new BufferedReader(new FileReader(filename_read));
				String line = "";
//				System.out.println(languages[langCount] + " model reading.....");

				while ((line = reader.readLine()) != null) {
					int lastSpaceIndex = line.lastIndexOf(" ");
					String strNgram = line.substring(0, lastSpaceIndex);
					float probabilityOfNgram = Float.parseFloat(line.substring(lastSpaceIndex + 1));
					map.put(strNgram, probabilityOfNgram);
				}
				reader.close();
				languageModel.put(languages[langCount],new HashMap<String, Float>(map));
				map.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return languageModel;
	}
	
	/*Function to lower the value of probabilities if they are about to cross the max possible value*/
	private static double[] lowerProbabilityValues(double langProbability[]){
		for(int i=0;i<languages.length;i++){
			langProbability[i] =langProbability[i] / maxDouble; 
		}
		return langProbability;
	}

	private static void identifyLanguage() {

		HashMap<String, HashMap<String, Float>> languageModel = getLanguageModel();
		char c = 'y';
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		try {
			while (c == 'y') {
				System.out.println("Enter some large text : ");
				String inputText = reader.readLine();
				while (inputText.equals("")) {
					System.out.println("Please enter some large text : ");
					inputText = reader.readLine();
				}
				double langProbability[] = new double[languages.length];
				NgramBuilder ngb = new NgramBuilder();
				ngb.createNgrams(n, inputText);
				HashMap<String, Integer> mapForInpText = ngb.getNgrams();

				Iterator<String> itForInpText = mapForInpText.keySet().iterator();
				while (itForInpText.hasNext()) {
					String ngKeyForInpText = (String) itForInpText.next();
					
					for (int langCount = 0; langCount < languages.length; langCount++) {
						if (languageModel.get(languages[langCount]).containsKey(ngKeyForInpText)) {
							float val = languageModel.get(languages[langCount]).get(ngKeyForInpText);
							// System.out.println(ngKeyForInpText + " is in " +
							// languages[langCount]
							// + " with probability " + val);
							if (langProbability[langCount] == 0.0) {
								langProbability[langCount] = val* mapForInpText.get(ngKeyForInpText);
							} else {
								if((langProbability[langCount] * val * mapForInpText.get(ngKeyForInpText)) > maxDouble){
//									System.out.println("Probability value too high : Might give wrong output");
									langProbability = lowerProbabilityValues(langProbability);
								}
								langProbability[langCount] *= (val * mapForInpText.get(ngKeyForInpText));
							}
						}
					}
				}
				int maxIndex = 0;
				for (int i = 0; i < languages.length; i++) {
//					System.out.println(langProbability[i] + " " + languages[i]);
					if (langProbability[i] > langProbability[maxIndex])
						maxIndex = i;
				}

				System.out.println("Detected Language : " + languages[maxIndex]);
				System.out.println("Continue (y/n)? ");
				String continueInp = reader.readLine();
				while (!(continueInp.equalsIgnoreCase("y") || continueInp.equalsIgnoreCase("n"))) {
					System.out.println("Continue (y/n)? ");
					continueInp = reader.readLine();
				}
				c = continueInp.charAt(0);
				langProbability = null;
				ngb = null;
				mapForInpText.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void preprocessTrainingData() {

		for (int langCount = 0; langCount < languages.length; langCount++) {

			String filename_read = training_directory + "/"	+ languages[langCount];

			String filename_write = test_directory + "/" + languages[langCount]	+ "_result.txt";

			NgramBuilder createTrainingSet = new NgramBuilder();

			try {
				// read from training data
				BufferedReader reader = new BufferedReader(new FileReader(filename_read));
				String line = "";
				HashMap<String, Integer> nGramMap = new HashMap<String, Integer>();

				System.out.println(languages[langCount] + " reading.....");

				while ((line = reader.readLine()) != null) {
					createTrainingSet.createNgrams(n, line);
				}

				nGramMap = createTrainingSet.getNgrams();
				reader.close();

				int totalNgrams = nGramMap.size();

				// write result set
				BufferedWriter writer = new BufferedWriter(new FileWriter(filename_write));
				System.out.println(languages[langCount] + " writing.....");
				Iterator<String> it = nGramMap.keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					float probability = (float) nGramMap.get(key) / totalNgrams;
					String probabilityString = String.format("%.9f",probability);
					line = key + " " + probabilityString;
					writer.write(line + "\n");
				}
				writer.close();

				System.out.println("Total n-grams : " + nGramMap.size());
				nGramMap.clear();
				createTrainingSet = null;

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(languages[langCount] + " done\n");
		}
	}
}
