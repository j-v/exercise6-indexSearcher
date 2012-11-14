import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/** Simple command-line based search demo. */
public class indexSearcher {

	/** Simple command-line based search demo. */
	public static void main(String[] args) throws Exception {
		String usage = "Usage:\njava -jar indexSearcher -index dir -query string -max <num of docs to show>";
		if (args.length > 0
				&& ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String indexPath = "";
		String field = "content";
		String queryString = null;
		int maxDocs = 100;

		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-query".equals(args[i])) {
				queryString = args[i + 1];
				i++;
			} else if ("-max".equals(args[i])) {
				try {
					maxDocs = Integer.parseInt(args[i + 1]);
					if(maxDocs <= 0) {
						System.out.println("Please use a >0 number for -max");
						System.exit(1);
					}
					i++;
				} catch (NumberFormatException e) {
					System.out.println("Please use a >0 number for -max");
					System.out.println(usage);
					System.exit(1);
				}
			}
		}

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);

		QueryParser parser = new QueryParser(Version.LUCENE_40, field, analyzer);
	
		if (queryString == null) { // prompt the user
			System.out.println("Please specify a query with -query");
			System.out.println(usage);
			System.exit(1);
		}

		queryString = queryString.trim();
		if (queryString.length() == 0) {
			System.out.println(usage);
			System.exit(1);
		}

		Query query = parser.parse(queryString);
		System.out.println("Searching for: " + query.toString(field));

		doPagingSearch(searcher, query, maxDocs);

		reader.close();
	}

	public static void doPagingSearch(IndexSearcher searcher, Query query, int maxDocs) throws IOException {

		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, maxDocs);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		for(int i = 0; i < numTotalHits && i < maxDocs; i++) {
			Document doc = searcher.doc(hits[i].doc);
			
			System.out.println("Document #" + (i+1) + " (score = " + hits[i].score + "):");
			System.out.println("\tURL: " + doc.get("url"));
			System.out.println("\tTILTE: " + doc.get("title"));
			System.out.println("\tCONTENT: " + doc.get("content"));
			System.out.println();
		}
	}
}
