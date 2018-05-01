
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                def scannerHome = "tool 'sonar_scanner'"
                withSonarQubeEnv('sonar') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
            }
        }
    }
}
