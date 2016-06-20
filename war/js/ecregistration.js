function onSelectType(tp) {
  var show = "bee-ec-registration-show";
  var hide = "bee-ec-registration-hide";

  var person = (tp == "1");

  var companyName = document.getElementById("CompanyName-field");
  if (companyName) {
    companyName.className = person ? hide : show;
  }
  var companyNameInput = document.getElementById("CompanyName-input");
  if (companyNameInput) {
    companyNameInput.disabled = person;
  }
  
  var companyCode = document.getElementById("CompanyCode-field");
  if (companyCode) {
    companyCode.className = person ? hide : show;
  }
  var companyCodeInput = document.getElementById("CompanyCode-input");
  if (companyCodeInput) {
    companyCodeInput.disabled = person;
  }

  var vatCode = document.getElementById("VatCode-field");
  if (vatCode) {
    vatCode.className = person ? hide : show;
  }
  var vatCodeInput = document.getElementById("VatCode-input");
  if (vatCodeInput) {
    vatCodeInput.disabled = person;
  }

  var personCode = document.getElementById("PersonCode-field");
  if (personCode) {
    personCode.className = person ? show : hide;
  }
  var personCodeInput = document.getElementById("PersonCode-input");
  if (personCodeInput) {
    personCodeInput.disabled = !person;
  }

  var vatPayerInput = document.getElementById("NotVATPayer-field");
  if (vatPayerInput) {
    vatPayerInput.className = person ? hide : show;
  }

  var companyType = document.getElementById("CompanyType-field");
  if (companyType) {
    companyType.className = person ? hide : show;
  }
  var companyTypeInput = document.getElementById("CompanyType-input");
  if (companyTypeInput) {
    companyTypeInput.disabled = person;
  }
}

function onSelectVAT() {
  var checkBox = document.getElementById("NotVATPayer");
  if (checkBox) {
    var value = checkBox.checked;
    var className = "bee-ec-registration-required";

    var vatCode = document.getElementById("VatCode-label");
    if (vatCode) {
      if (value) {
        vatCode.classList.remove(className);
      } else {
        vatCode.classList.add(className);
      }
    }

    var vatCodeInput = document.getElementById("VatCode-input");
    if (vatCodeInput) {
      vatCodeInput.required = !value;
    }
  }
}
