import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NgramBuilder {

	private HashMap<String, Integer> nGramsMap = new HashMap<String, Integer>();

	private List<String> ngrams(int n, String[] words) {
		// words array contains all the words in the line after removing punctuation
		List<String> ngramList = new ArrayList<String>();

		for (int i = 0; i < words.length; i++) {
			if (words[i].length() <= n) {
				ngramList.add(words[i]);
				continue;
			}
			concat(words[i], n, ngramList);
		}
		return ngramList;
	}

	private void concat(String word, int n, List<String> ngrams) {
		int maxIndex = word.length() - n;
		for (int j = 0; j <= maxIndex; j++) {
			ngrams.add(word.substring(j, j + n));
		}
	}

	public void createNgrams(int n,String str) {

		str = filterString(str);
		if (!str.equals("")) {
			String words[] = str.split(" ");

			for (String ngram : ngrams(n, words)) {

				if (!nGramsMap.containsKey(ngram)) {
					nGramsMap.put(ngram, 1);
				} else {
					int countOfNgram = nGramsMap.get(ngram);
					countOfNgram++;
					nGramsMap.remove(ngram);
					nGramsMap.put(ngram, countOfNgram);
				}
			}
		}
	}

	private String filterString(String line) {

		line = line.replaceAll("[0-9:,;()%.\"/!+$&@?*=]", ""); // removing punctuation
		line = line.replaceAll("\\t", " "); // replacing tabs with space
		while (line.indexOf("  ") >= 0){
			line = line.replaceAll("  ", " "); // replacing multiple spaces with single space
		}
		line = line.toLowerCase();
		return line;
	}

	public HashMap<String, Integer> getNgrams() {
		return nGramsMap;
	}
}
