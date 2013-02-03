<%@page import="com.butent.bee.shared.time.TimeUtils"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>to BEE or not to BEE</title>

<style type="text/css">
body {
  font-family: Arial, sans-serif;
  font-size: 12px;
  line-height: 1.5;
  background-color: white;
  color: #43494F;
}

.bee-SignIn-Panel {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 400px;
  height: 320px;
  margin-top: -160px;
  margin-left: -200px;
}

.bee-Error-Caption {
  color: red;
  position: absolute;
  left: 100px;
  bottom: 31%
}

.bee-SignIn-Caption {
  position: absolute;
  bottom: 20%;
  left: 119px;
}

.bee-SignIn-Label {
  top: 10px;
}

.bee-SignIn-Input {
  height: 32px;
  padding-left: 8px;
  border: 1px solid #d9d9d9;
  border-top: 1px solid silver;
  top: 30px;
  width: 175px;
  box-shadow: inset 3px 3px 10px silver;
  color: #43494F;
}

.bee-SignIn-User {
  left: 100px;
  position: absolute;
}

.bee-SignIn-Password {
  left: 100px;
  position: absolute;
}

.bee-SignIn-Button {
  height: 40px;
  width: 125px;
  padding-left: 8px;
  padding-right: 8px;
  position: absolute;
  left: 130px;
  top: 140px;
  background-color: #43494F;
  color: white;
  font-weight: bold;
  border: 2px solid #43494F;
  border-radius: 3px;
}

.bee-SignIn-Password-Label {
  top: 68px;
}

.bee-SignIn-Password-Input {
  height: 32px;
  padding-left: 8px;
  border: 1px solid #d9d9d9;
  border-top: 1px solid silver;
  top: 88px;
  width: 175px;
  box-shadow: inset 3px 3px 10px silver;
  color: #43494F
}

.bee-SignIn-Password-Input:focus {
  background-color: whitesmoke;
}

.bee-SignIn-Input:focus {
  background-color: whitesmoke;
}

.bee-SignIn-Button:hover {
  cursor: pointer;
  border-style: outset;
}

.bee-SignIn-Button:active {
  border-style: inset !important;
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
  <div class="bee-SignIn-Panel">
    <form method="post" action="j_security_check" accept-charset="UTF-8">
      <div style="position: absolute; left: 160px; top: -80px;">
        <img src="images/logo.gif" style="width: 55px; height: 76px;" border="0" />
      </div>

      <div class="bee-SignIn-Caption">
        UAB "Būtenta" &copy; 2010 - <%= TimeUtils.today().getYear() %>
      </div>
      
      <%  if (request.getParameter("fail") != null) { %>
        <div class="bee-Error-Caption">Bandykite dar kartą</div>
      <% } %>
      
      <div class="bee-SignIn-Label bee-SignIn-User">Prisijungimo vardas</div>
      <input type="text" class="bee-SignIn-Input bee-SignIn-User" name="j_username" id="user"
        onkeydown="return goPswd(event)" autofocus>

      <div class="bee-SignIn-Password-Label bee-SignIn-Password">Slaptažodis</div>
      <input type="password" class="bee-SignIn-Password-Input bee-SignIn-Password" name="j_password"
        id="pswd"> <input type="submit" class="bee-SignIn-Button" value="Prisijungti" />
    </form>
  </div>
</body>
</html>
