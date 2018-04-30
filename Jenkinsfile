// Powered by Infostretch 

timestamps {

node () {

	stage ('finesse - Checkout') {
 	 checkout([$class: 'GitSCM', branches: [[name: '*/demo']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'b74075c3-91e7-44f9-a050-056a21e81368', url: 'https://github.com/smashling/fitnesse_CI_DEMO']]]) 
	}
	stage ('finesse - Build') {
 	
withEnv(["JAVA_HOME=${ tool '"+JDK+"' }", "PATH=${env.JAVA_HOME}/bin"]) { 

// Unable to convert a build step referring to "hudson.plugins.ws__cleanup.PreBuildCleanup". Please verify and convert manually if required.
// Unable to convert a build step referring to "hudson.plugins.gradle.Gradle". Please verify and convert manually if required.
// Unable to convert a build step referring to "hudson.plugins.sonar.SonarRunnerBuilder". Please verify and convert manually if required.
		archiveArtifacts allowEmptyArchive: false, artifacts: 'build/libs/*.jar', caseSensitive: true, defaultExcludes: true, fingerprint: false, onlyIfSuccessful: false
		// JUnit Results
		junit '**/test-results/**/*.xml' 
	}
}
}
}