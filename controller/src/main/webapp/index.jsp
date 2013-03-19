<%
// TODO: retrieve these from a properties file
String serviceName = "Policy Placement Service";
String version = "0.0.3";
String prefix = request.getContextPath();
%>
<html>
<head>
  <title><%=serviceName%></title>
  <style>
  table {
    border: 0px;
    text-align: left;
    width: 800px;
  }
  td {
    padding: 2px 10px 2px 10px;
    border-collapse: collapse;
  }
  </style>
</head>
<body>
<h1><%=serviceName%></h1>
<h2>Version <%=version%></h2>
<hr/>
<p>Request Mappings</p>
<table>
  <tr>
    <th>Method</th>
    <th>URI</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>GET</td>
    <td><%=prefix%>/transfer/&lt;transfer-id&gt;</td>
    <td>Retrieves the status of an existing transfer in the policy session.</td>
  </tr>
  <tr>
    <td>POST</td>
    <td><%=prefix%>/transfer</td>
    <td>Creates a transfer in the policy session.</td>
  </tr>
  <tr>
    <td>PUT</td>
    <td><%=prefix%>/transfer/&lt;transfer-id&gt;</td>
    <td>Updates an existing transfer in the policy session.</td>
  </tr>
  <tr>
    <td>DELETE</td>
    <td><%=prefix%>/transfer/&lt;transfer-id&gt;</td>
    <td>Deletes an existing transfer from the policy session.</td>
  </tr>
  <tr>
    <td>GET</td>
    <td><%=prefix%>/transfer/list</td>
    <td>Retrieves a list of transfers in the policy session.</td>
  </tr>
  <tr>
    <td>POST</td>
    <td><%=prefix%>/transfer/list</td>
    <td>Creates a list of transfers in the policy session.</td>
  </tr>
  <tr>
    <td>PUT</td>
    <td><%=prefix%>/transfer/list</td>
    <td>Updates a list of transfers in the policy session.</td>
  </tr>
  <tr>
    <td>GET</td>
    <td><%=prefix%>/resource/list</td>
    <td>Retrieves the list of resources in the policy session.</td>
  </tr>
  <tr>
    <td>GET</td>
    <td><%=prefix%>/cleanup/list</td>
    <td>Retrieves the list of cleanups in the policy session.</td>
  </tr>
  <tr>
    <td>POST</td>
    <td><%=prefix%>/cleanup/list</td>
    <td>Creates a list of cleanups in the policy session.</td>
  </tr>
  <tr>
    <td>PUT</td>
    <td><%=prefix%>/cleanup/list</td>
    <td>Updates a list of existing cleanups in the policy session.</td>
  </tr>
    <tr>
    <td>GET</td>
    <td><%=prefix%>/cleanup/&lt;cleanup-ud&gt;</td>
    <td>Retrieves an existing cleanup from the policy session.</td>
  </tr>
  <tr>
    <td>PUT</td>
    <td><%=prefix%>/cleanup/&lt;cleanup-id&gt;</td>
    <td>Updates an existing cleanup in the policy session.</td>
  </tr>
  <tr>
    <td>GET</td>
    <td><%=prefix%>/global/&lt;variableName&gt;</td>
    <td>Retrieves a global variable object from the policy session.</td>
  </tr>
</table>
</body>
</html>