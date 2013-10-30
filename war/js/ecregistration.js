function onSelectType() {
  var show = "bee-ec-registration-show";
  var hide = "bee-ec-registration-hide";

  var person = document.getElementById("person").checked;

  var companyName = document.getElementById("CompanyName-field");
  if (companyName) {
    companyName.className = person ? hide : show;
  }

  var companyCode = document.getElementById("CompanyCode-field");
  if (companyCode) {
    companyCode.className = person ? hide : show;
  }

  var vatCode = document.getElementById("VatCode-field");
  if (vatCode) {
    vatCode.className = person ? hide : show;
  }

  var personCode = document.getElementById("PersonCode-field");
  if (personCode) {
    personCode.className = person ? show : hide;
  }
}