package QueryBuilder;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by murphy on 8/14/18.
 */
public class SpanQueryBuilder {
    protected objectHolder objectHolder = new objectHolder();
    protected Pattern quotes = Pattern.compile("\"[^\"(/]*\"");
    protected Pattern parenthesis = Pattern.compile("\\([^)(]*\\)");
    protected Pattern within = Pattern.compile("/[0-9]*");
    protected Pattern wildcard = Pattern.compile("\\*");
    protected Pattern space = Pattern.compile(("[\\s]"));
    public SpanQuery getQuery(Analyzer anal, String fieldName, String query, IndexReader ir) throws Exception {
        String code = makeQuery(anal, fieldName, query.replaceAll("\\)\\(",") ("), ir);

        SpanQuery sq = objectHolder.getSpanQueryFromCode(code);
        System.out.println(sq.toString());
        return sq;
    }

    String makeQuery(Analyzer anal, String fieldName, String query, IndexReader ir) throws Exception {
        if(objectHolder.isCode(query))
            return query;
        while(true){
            Matcher m = quotes.matcher(query);
            if(!m.find())
                break;
            String sub = query.substring(m.start()+1,m.end()-1);

            String replacement = processString(anal, fieldName, sub, ir);
            query = m.replaceFirst(replacement);
        }
        while(true){
            Matcher m = parenthesis.matcher(query);
            if(!m.find())
                break;
            String sub = query.substring(m.start()+1,m.end()-1);
            String replacement = makeQuery(anal, fieldName, sub, ir);
            query = m.replaceFirst(replacement);
            query = makeQuery(anal,fieldName, query, ir);
        }
        while(true){
            Matcher m = within.matcher(query);
            if(!m.find())
                break;
            String srange = query.substring(m.start()+1, m.end());
            int range = Integer.parseInt(srange);
            String first = query.substring(0,m.start()-1);
            String second = query.substring(m.end()+1, query.length());
            String firsteplacement = makeQuery(anal, fieldName, first, ir);
            String secondreplacement = makeQuery(anal, fieldName, second, ir);
            SpanQuery sqFirst = objectHolder.getSpanQueryFromCode(firsteplacement);
            SpanQuery sqSecond = objectHolder.getSpanQueryFromCode(secondreplacement);
            SpanNearQuery snq = new SpanNearQuery(new SpanQuery[] {sqFirst, sqSecond}, range, false);
            String code = objectHolder.addSpanQuery(snq);

            query = code;
            query = makeQuery(anal,fieldName, query, ir);
        }
        //String
        query = query.trim();
        String[] subqueries = space.split(query);
        if(subqueries.length==1){
            //query1 = makeQuery(anal, fieldName, query,ir);
            query = processString(anal, fieldName, query, ir);
        }
        if(subqueries.length > 1){
            SpanQuery[] codes = new SpanQuery[subqueries.length];
            for(int i=0;i<subqueries.length;i++){
                codes[i] = objectHolder.getSpanQueryFromCode(processString(anal, fieldName, subqueries[i], ir));

            }
            SpanOrQuery soq = new SpanOrQuery(codes);
            String ret = objectHolder.addSpanQuery(soq);
            query = makeQuery(anal, fieldName, ret, ir);

        }
        return query;
        //return  processString(anal, fieldName, query, ir);




    }
    protected String getAnalyzed(Analyzer anal, String fieldName, String query, IndexReader ir){


        ArrayList<String> toks = new ArrayList<>();
        TokenStream ts = anal.tokenStream(fieldName, query);


        CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);
        try {
            ts.reset();
            while(ts.incrementToken()){
                toks.add(ch.toString());

            }
            ts.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        StringBuilder sb = new StringBuilder();
        Boolean isFirst = true;
        for(String tok:toks){
            if(isFirst)
                isFirst = false;
            else
                sb.append(" ");
            sb.append(tok);
        }

        return sb.toString();
    }


    String processString(Analyzer anal, String fieldName, String query, IndexReader ir) throws Exception {

        //if(query.contains("*"))
        //    return objectHolder.addSpanQuery(makeWildCardSpanQuery(anal, fieldName, query, ir));

        if(objectHolder.isCode(query))
            return query;
        String[] toks = query.split("[\\s]");
        for(int i=0;i<toks.length;i++) {
            String ganal = getAnalyzed(anal, fieldName, toks[i], ir);
            if (ganal.contains(" ")) {
                String rganal = ganal.replace(" ", ".");
                toks[i] = toks[i].replaceAll(rganal, ganal);
                toks[i] = processString(anal,fieldName,toks[i],ir);

            }
        }


        if(toks.length < 1)
            throw new Exception("No tokens in stream " + query);
        if(toks.length == 1) {
                if(objectHolder.isCode(toks[0]))
                    return toks[0];
                return objectHolder.addSpanQuery(new SpanTermQuery(new Term(fieldName, toks[0])));
        }
        else {
            SpanQuery[] sq = new SpanQuery[toks.length];
            for(int i=0;i<toks.length;i++){
                if(objectHolder.isCode(toks[i]))
                    sq[i] = objectHolder.getSpanQueryFromCode(toks[i]);
                else
                    sq[i] = new SpanTermQuery(new Term(fieldName,toks[i]));
            }
            SpanNearQuery snq = new SpanNearQuery(sq, 0, true);
            return objectHolder.addSpanQuery(snq);
        }

    }
    class objectHolder{
        private HashMap<String, SpanQuery> hashmap;
        private int count = 0;
        public objectHolder(){
            hashmap = new HashMap();
        }
        public String addSpanQuery(SpanQuery sq){
            if(sq.getClass().toString().equals("class org.apache.lucene.search.spans.SpanTermQuery")){
                SpanTermQuery soq = (SpanTermQuery)sq;
                if(soq.getTerm().text().length()==0)
                    return null;
            }
            String code = String.format("zz%012dzz", count);
            hashmap.put(code, sq);

            count++;
            return code;
        }
        public SpanQuery getSpanQueryFromCode(String code){
            return hashmap.get(code);
        }
        public Boolean isCode(String code){
            return hashmap.containsKey(code);
        }

    }
}