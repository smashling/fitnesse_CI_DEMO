node {
  stage('SonarQube analysis') {
    withSonarQubeEnv('sonar') {
      // requires SonarQube Scanner for Gradle 2.1+
      // It's important to add --info because of SONARJNKNS-281
      sh './gradlew --info sonarqube'
    }
  }
}
