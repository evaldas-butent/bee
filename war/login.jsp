<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html xmlns="[http://www.w3.org/1999/xhtml" ] xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>to BEE or not to BEE</title>

<style type="text/css">
body, select {
  font-family: Arial, sans-serif;
  font-size: small;
}
.bee-SignIn-Popup {
  background-color: whitesmoke;
  border: 1px solid #e5e5e5;
}
.bee-SignIn-Panel {
  width: 400px;
  height: 320px;	
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
.bee-SignIn-Language {
  font-size: 15px;
  position: absolute;
  left: 20px;
  top: 220px;
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
.bee-RadioButton input:checked + label {
  text-transform: uppercase;
}
</style>
</head>
<body>
<div style="position: absolute; overflow-x: visible; overflow-y: visible; left: 441px; top: 280px; visibility: visible; " class="bee-SignIn-Popup" id="popup-2">
<div class="popupContent">	
<div
		style="overflow-x: hidden; overflow-y: hidden; position: relative;"
		id="absolute-3" class="bee-SignIn-Panel">
	<form method="post" action="j_security_check">
		<div class="bee-Label bee-SignIn-Caption" id="lbl-5">Būtent CRM</div>
		<div class="bee-Label bee-SignIn-Label bee-SignIn-User" id="lbl-7">Prisijungimo
			vardas</div>
		<input type="text" id="txt-8" name="j_username"
			class="bee-InputText bee-SignIn-Input bee-SignIn-User">
		<div class="bee-Label bee-SignIn-Label bee-SignIn-Password"
			id="lbl-10">Slaptažodis</div>
		<input type="password" id="pswd-11" name="j_password"
			class="bee-InputPassword bee-SignIn-Input bee-SignIn-Password"><span
			id="rg-12" class="bee-RadioGroup bee-SignIn-Language"><span
			class="bee-RadioButton bee-RadioButton-horizontal" id="rb-13"><input
				type="radio" name="optiongroup1" value="0" id="gwt-uid-1"
				tabindex="0" checked=""><label for="gwt-uid-1">lt</label></span><span
			class="bee-RadioButton bee-RadioButton-horizontal" id="rb-14"><input
				type="radio" name="optiongroup1" value="1" id="gwt-uid-2"
				tabindex="0"><label for="gwt-uid-2">lv</label></span><span
			class="bee-RadioButton bee-RadioButton-horizontal" id="rb-15"><input
				type="radio" name="optiongroup1" value="2" id="gwt-uid-3"
				tabindex="0"><label for="gwt-uid-3">et</label></span><span
			class="bee-RadioButton bee-RadioButton-horizontal" id="rb-16"><input
				type="radio" name="optiongroup1" value="3" id="gwt-uid-4"
				tabindex="0"><label for="gwt-uid-4">en</label></span><span
			class="bee-RadioButton bee-RadioButton-horizontal" id="rb-17"><input
				type="radio" name="optiongroup1" value="4" id="gwt-uid-5"
				tabindex="0"><label for="gwt-uid-5">de</label></span><span
			class="bee-RadioButton bee-RadioButton-horizontal" id="rb-18"><input
				type="radio" name="optiongroup1" value="5" id="gwt-uid-6"
				tabindex="0"><label for="gwt-uid-6">ru</label></span><span
			class="bee-RadioButton bee-RadioButton-horizontal" id="rb-19"><input
				type="radio" name="optiongroup1" value="6" id="gwt-uid-7"
				tabindex="0"><label for="gwt-uid-7">pl</label></span></span>
		<input type="submit" class="bee-SignIn-Button" id="bu-20" value="Prisijungti" />
      </form>
	</div>
</div>
</div>
</body>
</html>
