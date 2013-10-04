<%@ page import="com.butent.bee.shared.time.TimeUtils" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
  height: 400px;
  margin-top: -240px;
  margin-left: -200px;
}

.bee-SignIn-Logo-container {
  position: absolute;
  top: -150px;
  left: 25%;
  width: 185px;
  height: 100%;
  background-image: url("images/logo.png");
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
}

.bee-SignIn-Label {
  position: absolute;
  left: 100px;
}

.bee-SignIn-Input {
  position: absolute;
  left: 100px;
  width: 175px;
  height: 32px;
  padding-left: 8px;
  border: 1px solid #d9d9d9;
  border-top: 1px solid silver;
  box-shadow: inset 3px 3px 10px silver;
  color: #43494F;
}

.bee-SignIn-Input:focus {
  background-color: whitesmoke;
}

.bee-SignIn-Label-user {
  top: 90px;
}

.bee-SignIn-Input-user {
  top: 110px;
}

.bee-SignIn-Label-password {
  top: 148px;
}

.bee-SignIn-Input-password {
  top: 168px;
}

.bee-SignIn-Button {
  position: absolute;
  left: 130px;
  top: 220px;
  height: 40px;
  width: 125px;
  padding-left: 8px;
  padding-right: 8px;
  background-color: #43494F;
  color: white;
  font-weight: bold;
  border: 2px solid #43494F;
  border-radius: 3px;
  cursor: pointer;
}

.bee-SignIn-Input:focus,
.bee-SignIn-Button:focus {
  outline: #80c4f9 solid 1px;
}

.bee-SignIn-Error {
  position: absolute;
  left: 100px;
  bottom: 100px;
  color: red;
}

.bee-SignIn-Caption {
  position: absolute;
  left: 105px;
  bottom: 64px;
}

.bee-Copyright-logo {
  max-height: 16px;
  max-width: 16px;
  width: auto;
  height: auto;
  margin-bottom: -4px
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
      <div class="bee-SignIn-Logo-container"></div>

      <div class="bee-SignIn-Label bee-SignIn-Label-user">Prisijungimo vardas</div>
      <input type="text" class="bee-SignIn-Input bee-SignIn-Input-user" name="j_username" id="user"
        onkeydown="return goPswd(event)" autofocus>

      <div class="bee-SignIn-Label bee-SignIn-Label-password">Slaptažodis</div>
      <input type="password" class="bee-SignIn-Input bee-SignIn-Input-password" name="j_password"
        id="pswd">

      <input type="submit" class="bee-SignIn-Button" value="Prisijungti" />

      <%  if (request.getParameter("fail") != null) { %>
        <div class="bee-SignIn-Error">Bandykite dar kartą</div>
      <% } %>

      <div class="bee-SignIn-Caption"><img id="img-36" class="bee-Copyright-logo" src="images/logo.gif"> UAB "Būtenta" &copy; 2010 - <%= TimeUtils.today().getYear() %></div>
    </form>
  </div>
</body>
</html>
