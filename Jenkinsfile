pipeline {
  agent any
  stages {
  //stage('SCM') {
   // git 'https://github.com/foo/bar.git'
  //}
  stage('SonarQube analysis') {
    withSonarQubeEnv('My SonarQube Server') {

     sh './gradlew --info sonarqube'
    }
  }
 }
}
