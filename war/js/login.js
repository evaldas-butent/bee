function browserSupported() {
  return typeof document.body.style.flex == "string" 
      || typeof document.body.style.msFlex == "string" 
      || typeof document.body.style.webkitFlex == "string";
}

function goPswd(ev) {
  if (ev.keyCode && ev.keyCode == 13) {
    var p = document.getElementById("pswd");
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

function getSelectedLanguage() {
  var nodes = document.forms["login"]["locale"];
  for (var i = 0; i < nodes.length; i++) {
    var node = nodes.item(i);
    if (node.checked) {
      return node.value;
    }
  }
  return null;
}
function setSelectedLanguage(form) {
  form["locale"].value = getSelectedLanguage();
}

function translate(lng) {
  var dictionary = eval("dictionary" + lng);
  if (dictionary) {
    for ( var id in dictionary) {
      var el = document.getElementById(id);
      if (el) {
        el.textContent = dictionary[id];
      }
    }
  }
}

function onload(reqLng) {
  if (browserSupported()) {
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

    document.body.className = "bee-ready";
    
    var u = document.getElementById("user");
    if (u) {
      u.focus();
    }

  } else {
    showSupport();
  }
}

function showSupport() {
  document.body.className = "bee-not-supported";
  
  var html = 
    "<h3>Your browser is not supported</h3>" +
    "<div>B-NOVO is currently supported only through the following browsers:</div>" +
    "<ul>" +
    '<li><a href="http://www.google.com/chrome">Chrome 30+</a></li>' +
    '<li><a href="http://www.mozilla.com/firefox">Firefox 23+</a></li>' +
    '<li><a href="http://windows.microsoft.com/en-us/internet-explorer/download-ie">Internet Explorer 10+</a></li>' +
    '<li><a href="http://www.opera.com">Opera 17+</a></li>' +
    '<li><a href="http://www.apple.com/safari">Safari 7+</a></li>' +
    "</ul>";
  
  document.body.innerHTML = html;
}