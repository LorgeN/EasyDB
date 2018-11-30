node ("master") {
  stage ('Build') {
 
    git url: 'https://github.com/LorgeN/EasyDB'
 
    def mvn_version = 'M3'
    withEnv( ["PATH+MAVEN=${tool mvn_version}/bin"] )
      bat "mvn clean install"
    }
  }
}
