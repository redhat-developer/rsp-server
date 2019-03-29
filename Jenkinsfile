#!/usr/bin/env groovy

node('rhel7') {
	stage('Checkout SCM') {
		deleteDir()
		git url: 'https://github.com/redhat-developer/rsp-server'
	}

	stage('Build') {
		def mvnHome = tool 'maven3-latest'
		env.PATH="${env.PATH}:${mvnHome}/bin"

		sh 'mvn -B clean install -Pintegration-tests'
		sh 'mvn -B clean package -f distribution/distribution/pom.xml'
		sh 'mvn -B clean compile exec:java package -f api/docs/org.jboss.tools.rsp.schema/pom.xml'

		junit '**/integration-tests/target/surefire-reports/*.xml,**/tests/**/target/surefire-reports/*.xml'
		archiveArtifacts artifacts: 'distribution/distribution/target/org.jboss.tools.rsp.distribution-*.zip,api/docs/org.jboss.tools.rsp.schema/target/*.jar,**/integration-tests/target/surefire-reports/*,**/tests/**/target/surefire-reports/*'
	}
	
	stage('SonarCloud Report') {
		def mvnHome = tool 'maven3-latest'
		env.PATH="${env.PATH}:${mvnHome}/bin"

		sh 'mvn -B -P sonar sonar:sonar -Dsonar.login=${SONAR_TOKEN}'
	}

	stage('Coverage Report') {
		sh '''#!/bin/bash
			bash <(curl -s https://codecov.io/bash) -f target/jacoco-report/jacoco.xml || echo "Codecov did not collect coverage reports"
		'''
	}

	stage('Snapshot') {
		def filesToPush = findFiles(glob: '**/*.zip')
		sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${filesToPush[0].path} ${UPLOAD_LOCATION}/snapshots/"
	}
}
