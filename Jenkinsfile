node ("master") {
  stage ('Clone') {
    deleteDir()
    git url: 'https://github.com/LorgeN/EasyDB'
  }
  stage ('Build') {
    env.JAVA_HOME="${tool 'JDK8'}"
    env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
    def mvn_version = 'M3'
    withEnv( ["PATH+MAVEN=${tool mvn_version}/bin"] ) {
      bat "mvn clean install"
    }
  }
  stage('Publish') {
    bat 'mvn deploy'
  }
}
