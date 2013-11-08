function browserSupported() {
  return typeof document.body.style.flex == "string" || typeof document.body.style.msFlex == "string";
}

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

function getStoredLanguage() {
  return localStorage.getItem("language");
}
function storeLanguage(lng) {
  localStorage.setItem("language", lng);
}

function onSelectLanguage(lng) {
  if (lng) {
    storeLanguage(lng);
    translate(lng);
  }
}

function translate(lng) {
  var dictionary = eval("dictionary" + lng);
  if (dictionary) {
    for (var id in dictionary) {
      var el = document.getElementById(id);
      if (el) {
        el.textContent = dictionary[id];
      }
    }
  }
}

function onload(reqLng) {
  var lng = getStoredLanguage();
  if (!lng) {
    lng = reqLng;
  }

  if (lng) {
    var el = document.getElementById(lng);
    if (el) {
      el.checked = true;
    }
    translate(lng);
  }
}