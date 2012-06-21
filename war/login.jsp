<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>to BEE or not to BEE</title>

<style type="text/css">
body {
  font-family: Arial, sans-serif;
  font-size: small;
}
.bee-SignIn-Popup {
  position:absolute;
  top:0;
  left:0;
  right:0;
  bottom:0;
  display: box;
  display: -moz-box;
  display: -webkit-box;
  box-pack: center;
  box-align: center;
  -moz-box-pack: center;
  -moz-box-align: center;
  -webkit-box-pack: center;
  -webkit-box-align: center;
}
.bee-SignIn-Panel {
  background-color: whitesmoke;
  border: 1px solid #e5e5e5;
  width: 400px;
  height: 320px;    
}
.bee-Error-Caption {
  font-size: 16px;
  color: red;
  position: absolute;
  right: 20px;
  top: 20px;
}
.bee-SignIn-Caption {
  font-size: 16px;
  color: #222;
  position: absolute;
  left: 20px;
  top: 20px;
}
.bee-SignIn-Label {
  font-size: 13px;
  font-weight: bold;
  color: #222;
  position: absolute;
  right: 220px;
}
.bee-SignIn-Input {
  font-size: 15px;
  height: 32px;
  padding-left: 8px;
  border: 1px solid #d9d9d9;
  border-top: 1px solid silver;
  background-color: #faffbd;
  position: absolute;
  left: 200px;
  right: 20px;
}
.bee-SignIn-User {
  top: 80px;
}
.bee-SignIn-Password {
  top: 140px;
}
.bee-SignIn-Button {
  font-size: 13px;
  font-weight: bold;
  height: 32px;
  padding-left: 8px;
  padding-right: 8px;
  color: white;
  background-color: #4d90fe;
  position: absolute;
  right: 20px;
  bottom: 20px;
}
</style>
<script type="text/javascript">
function goPswd(ev) {
  if (ev.keyCode && ev.keyCode == 13) {
    var p = document.getElementById('pswd');
    if (p) {
      p.focus();
      return false;
    }
  }
  return true;
}
</script>
</head>
<body>
<div style="position: absolute; left: 20px; top: 20px;">
  <img src="images/logo.gif" style="width: 55px; height: 76px;" border="0" />
</div>
<div class="bee-SignIn-Popup">
  <div class="bee-SignIn-Panel" style="overflow-x: hidden; overflow-y: hidden; position:relative;">
    <form method="post" action="j_security_check" accept-charset="UTF-8">
    <div class="bee-SignIn-Caption">Būtent CRM</div>
<%
if (request.getParameter("fail") != null) {
%>
    <div class="bee-Error-Caption">Bandykite dar kartą</div>
<%
}
%>
    <div class="bee-SignIn-Label bee-SignIn-User">Prisijungimo vardas</div>
    <input type="text" class="bee-SignIn-Input bee-SignIn-User" name="j_username" id="user"
      onkeydown="return goPswd(event)" autofocus>
    <div class="bee-SignIn-Label bee-SignIn-Password">Slaptažodis</div>
    <input type="password" class="bee-SignIn-Input bee-SignIn-Password" name="j_password" id="pswd">
    <input type="submit" class="bee-SignIn-Button" value="Prisijungti" />
    </form>
  </div>
</div>
</body>
</html>
