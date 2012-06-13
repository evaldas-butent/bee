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
