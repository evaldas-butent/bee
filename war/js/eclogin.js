function ecCommandRegister() {
  document.forms["register"]["locale"].value = getSelectedLanguage(); 
  return true;
}