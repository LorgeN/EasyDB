node ("master") {
  stage ('Build') {
 
    git url: 'https://github.com/LorgeN/EasyDB'
 
    withMaven() {
      bat "mvn clean install"
    }
  }
}
