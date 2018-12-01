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
    def pom = readMavenPom file: 'pom.xml'
    nexusPublisher nexusInstanceId: 'CP-Nexus', \
    nexusRepositoryId: 'maven-snapshots', \
    packages: [[$class: 'MavenPackage', \
                mavenAssetList: [[classifier: '', \
                                  extension: '', \
                                  filePath: 'target/${pom.artifactId}-${pom.version}.${pom.packaging}']], \
                mavenCoordinate: [artifactId: '${pom.artifactId}', \
                                  groupId: '${pom.groupId}', \
                                  packaging: '${pom.packaging}', \
                                  version: '${pom.version}']]]
  }
}
