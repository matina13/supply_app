<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
if (session == null) response.sendRedirect("/");
else {
    if (session.getAttribute("role") == null || !session.getAttribute("role").equals("admin")) response.sendRedirect("/");
}
%>

<h1>Welcome to Admin Dashboard.</h1>