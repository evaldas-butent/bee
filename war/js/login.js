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
