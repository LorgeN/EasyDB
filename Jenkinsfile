node ("master") {
  stage ('Clone') {
    deleteDir()
    git url: 'https://github.com/LorgeN/EasyDB'
  }
  stage ('Build') {
    env.PATH="${tool 'JDK8'}/bin:${env.PATH}"
    env.PATH="${tool 'M3'}/bin:${env.PATH}"
    
    bat "mvn clean install"
  }
  stage('Publish to Snapshots') {
    configFileProvider(
        [configFile(fileId: '8ef00a76-7b90-4537-acd3-906fb7af45a0', variable: 'MAVEN_SETTINGS')]) {
        bat 'mvn -s $MAVEN_SETTINGS deploy'
    }
  }
}
