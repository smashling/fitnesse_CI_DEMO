
pipeline {
    environment {
        def scannerHome = "tool 'sonar_scanner'"
    agent any
    stages 
      stage('Build') {
          steps {
                 withSonarQubeEnv('sonar') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
            }
        }
    }
}
