function trCommandRegister() {
  document.forms["register"]["locale"].value = getSelectedLanguage(); 
  return true;
}

function trCommandQuery() {
  document.forms["query"]["locale"].value = getSelectedLanguage(); 
  return true;
}