<%@ page import="java.util.ArrayList" %><%--
  Created by IntelliJ IDEA.
  User: ruisun
  Date: 11/12/20
  Time: 23:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>Oxford Dictionary Explanation Administration Platform</title>
  </head>
  <body>
  <h1>Analytical Page</h1>
  <h3>Total Count of search: <%= request.getAttribute("count") %></h3>
  <h3>API request success rate: <%= request.getAttribute("rate") %></h3>
  <h3>Average response time: <%= request.getAttribute("time") %> ms</h3>

  <h4>LOGS:</h4>
  <% for (String s: (ArrayList<String>) request.getAttribute("logs")) { %>
  <p><%= s %></p>
  <% } %>
  </body>
</html>
