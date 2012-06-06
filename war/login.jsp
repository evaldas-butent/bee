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
  background-color: whiteSmoke;
  border: 1px solid #EDEAEA;
  width: 400px;
  height: 320px;
  color: #6B6B6D;
  font: 13px Arial;
  border-radius: 2px;	
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
  color: #A7A7A7;
  position: absolute;
  left: 20px;
  top: 20px;
}
.bee-SignIn-Label {
  font-size: 13px;
 /* font-weight: bold;
  color: #222; */
  position: absolute;
  right: 220px;
}
.bee-SignIn-Input {
  font-size: 13px;
  font: 13px Arial;
  /* height: 32px; 
  padding-left: 8px; */
  border: 1px solid #D8D7D7;
  /* border-top: 1px solid silver; */
  /* background-color: #faffbd; */
  position: absolute;
  left: 200px;
  right: 20px;
  border-radius: 2px;
  padding: 7px;
}
.bee-SignIn-User {
  top: 80px;
}
.bee-SignIn-Password {
  top: 140px;
}
.bee-SignIn-Language {
  font-size: 15px;
  position: absolute;
  left: 20px;
  top: 220px;
}
.bee-SignIn-Button {
  font-size: 14px;
  font-weight: bold; 
  /* height: 32px; 
  padding-left: 8px;
  padding-right: 8px; */
  padding: 10px 25px;
  color: white;
  background-color: #878586;
  position: absolute;
  right: 20px;
  bottom: 20px;
  border-radius: 2px;
  border: 0;
  cursor: pointer;
}
.bee-RadioButton input:checked + label {
  text-transform: uppercase;
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
<div style="overflow: hidden; position: absolute; left: 20px; right: 0px; top: 20px; bottom: 0px; " id="complex-11" class="bee-NorthContainer">
  <img src="data:image/gif;base64,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" style="width: 55px; height: 76px;" border="0" />
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
    <span class="bee-RadioGroup bee-SignIn-Language">
      <span class="bee-RadioButton bee-RadioButton-horizontal">
        <input type="radio" name="lang" value="0" id="lt" checked="checked"><label for="lt">lt</label>
      </span>
      <span class="bee-RadioButton bee-RadioButton-horizontal">
        <input type="radio" name="lang" value="1" id="lv"><label for="lv">lv</label>
      </span>
      <span class="bee-RadioButton bee-RadioButton-horizontal">
        <input type="radio" name="lang" value="2" id="et"><label for="et">et</label>
      </span>
      <span class="bee-RadioButton bee-RadioButton-horizontal">
        <input type="radio" name="lang" value="3" id="en"><label for="en">en</label>
      </span>
      <span class="bee-RadioButton bee-RadioButton-horizontal">
        <input type="radio" name="lang" value="4" id="de"><label for="de">de</label>
      </span>
      <span class="bee-RadioButton bee-RadioButton-horizontal">
        <input type="radio" name="lang" value="5" id="ru"><label for="ru">ru</label>
      </span>
      <span class="bee-RadioButton bee-RadioButton-horizontal">
        <input type="radio" name="lang" value="6" id="pl"><label for="pl">pl</label>
      </span>
    </span>
    <input type="submit" class="bee-SignIn-Button" value="Prisijungti" />
    </form>
  </div>
</div>
</body>
</html>
