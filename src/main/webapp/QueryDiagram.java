package QueryBuilder;

import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

/**
 * Created by murphy on 8/17/18.
 */
public class QueryDiagram {
    public static void  diagram(SpanQuery sq){
        int level = 1;
        System.out.println("{ \"query\":{");
        diagram(sq,level);
        System.out.println("}}");
    }
    private static void diagram(SpanQuery sq, int level){

        if(sq.getClass().toString().equals("class org.apache.lucene.search.spans.SpanOrQuery")){
            SpanOrQuery soq = (SpanOrQuery)sq;
            printLevel(level);
            System.out.println("\"span_or\": { \"clauses\": [");
            boolean isFirst = true;
            for(SpanQuery sub:soq.getClauses()) {
                if (isFirst)
                    isFirst = false;
                else
                    System.out.print(",");
                System.out.print("{");
                diagram(sub, level + 1);
                System.out.print("}");
            }
            printLevel(level);
            System.out.println("]}");
        }
        if(sq.getClass().toString().equals("class org.apache.lucene.search.spans.SpanNearQuery")){
            SpanNearQuery soq = (SpanNearQuery)sq;
            printLevel(level);
            boolean isFirst = true;
            System.out.println("\"span_near\": { \"clauses\": [");
            for(SpanQuery sub:soq.getClauses()) {
                if (isFirst)
                    isFirst = false;
                else
                    System.out.print(",");
                System.out.print("{");
                diagram(sub, level + 1);
                System.out.print("}");
            }
            printLevel(level);

            System.out.println( "] , \"slop\": " + soq.getSlop() + ",\"in_order\":" + soq.isInOrder() + "}");
        }
        if(sq.getClass().toString().equals("class org.apache.lucene.search.spans.SpanTermQuery")){
            SpanTermQuery soq = (SpanTermQuery)sq;
            printLevel(level);
            if(soq.getTerm().text().contains("*"))
            {
                System.out.println("\"span_multi\": { \"match\": { \"wildcard\" : {\""
                        + soq.getField()
                        + "\" : \""
                        + soq.getTerm().text()
                        + "\"}}}" );
            }
            else
            {
                System.out.println("\"span_term\" : {\""
                        + soq.getField()
                        + "\":\""
                        + soq.getTerm().text()
                        + "\"}" );
            }
            for(int i=0;i<level*2;i++)
                System.out.print(" ");
            System.out.println("");
        }

    }
    private static void printLevel(int level){
        for(int i=0;i<level*2;i++){
            System.out.print(" ");
        }
    }
}
