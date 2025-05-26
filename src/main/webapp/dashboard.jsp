<%
if (session == null) response.sendRedirect("/");
else {
    if (session.getAttribute("role") == null) response.sendRedirect("/");
    else if (!session.getAttribute("role").equals("user") && !session.getAttribute("role").equals("plus")) response.sendRedirect("/");
}
%>

hello
<br>
<a href="app.jsp">App</a>
