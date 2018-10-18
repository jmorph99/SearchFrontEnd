<%-- 
    Document   : query
    Created on : Oct 17, 2018, 5:26:25 AM
    Author     : murphy
--%>

<%@page import="org.apache.lucene.analysis.standard.StandardAnalyzer"%>
<%@page import="QueryBuilder.QueryDiagram"%>
<%@page import="org.apache.lucene.search.spans.SpanQuery"%>
<%@page import="QueryBuilder.SpanQueryBuilder"%>
<%@page import="org.apache.lucene.analysis.CharArraySet"%>
<%@page import="org.apache.lucene.analysis.Analyzer"%>
<%@page import="java.io.BufferedReader"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% 
StringBuilder sb = new StringBuilder();
if (request.getContentLength() > 0) {
      String line;
      
      try {
         BufferedReader rd = new BufferedReader(request.getReader());
       	while ((line = rd.readLine()) != null) {
            // Process line...
            sb.append(line);
       	}
       	rd.close();
      } catch (Exception e) { e.printStackTrace(); }

Analyzer anal = new StandardAnalyzer((CharArraySet) null);
            SpanQueryBuilder sqb = new SpanQueryBuilder();
            SpanQuery sq = sqb.getQuery(anal,"content", sb.toString(), null);
            response.getWriter().print(QueryDiagram.diagram(sq));
            
        }
%>