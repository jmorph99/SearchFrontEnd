package QueryBuilder;

import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

/**
 * Created by murphy on 8/17/18.
 */
public class QueryDiagram {
    public static StringBuilder sb;
    public static String  diagram(SpanQuery sq){
        sb = new StringBuilder();
        int level = 1;
        sb.append("{ \"query\":{");
        diagram(sq,level);
        sb.append("},\n" +
"  \"highlight\": {\n" +
"    \"fields\": {\n" +
"      \n" +
"      \"content\" : {}},\"require_field_match\": true,\"pre_tags\": [\"<b>\"],\"post_tags\": [\"</b>\"]\n" +
"    \n" +
"  }}");
        return sb.toString();
    }
    private static void diagram(SpanQuery sq, int level){

        if(sq.getClass().toString().equals("class org.apache.lucene.search.spans.SpanOrQuery")){
            SpanOrQuery soq = (SpanOrQuery)sq;
            printLevel(level);
            sb.append("\"span_or\": { \"clauses\": [");
            boolean isFirst = true;
            for(SpanQuery sub:soq.getClauses()) {
                if (isFirst)
                    isFirst = false;
                else
                    sb.append(",");
                sb.append("{");
                diagram(sub, level + 1);
                sb.append("}");
            }
            printLevel(level);
            sb.append("]}");
        }
        if(sq.getClass().toString().equals("class org.apache.lucene.search.spans.SpanNearQuery")){
            SpanNearQuery soq = (SpanNearQuery)sq;
            printLevel(level);
            boolean isFirst = true;
            sb.append("\"span_near\": { \"clauses\": [");
            for(SpanQuery sub:soq.getClauses()) {
                if (isFirst)
                    isFirst = false;
                else
                    sb.append(",");
                sb.append("{");
                diagram(sub, level + 1);
                sb.append("}");
            }
            printLevel(level);

            sb.append( "] , \"slop\": " + soq.getSlop() + ",\"in_order\":" + soq.isInOrder() + "}");
        }
        if(sq.getClass().toString().equals("class org.apache.lucene.search.spans.SpanTermQuery")){
            SpanTermQuery soq = (SpanTermQuery)sq;
            printLevel(level);
            if(soq.getTerm().text().contains("*"))
            {
                sb.append("\"span_multi\": { \"match\": { \"wildcard\" : {\""
                        + soq.getField()
                        + "\" : \""
                        + soq.getTerm().text()
                        + "\"}}}" );
            }
            else
            {
                sb.append("\"span_term\" : {\""
                        + soq.getField()
                        + "\":\""
                        + soq.getTerm().text()
                        + "\"}" );
            }
            for(int i=0;i<level*2;i++)
                sb.append(" ");
            sb.append("");
        }

    }
    private static void printLevel(int level){
        for(int i=0;i<level*2;i++){
            sb.append(" ");
        }
    }
}
