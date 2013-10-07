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
