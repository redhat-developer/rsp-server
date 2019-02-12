#!/usr/bin/env groovy

node('rhel7') {
	stage('Checkout SCM') {
		deleteDir()
		git url: 'https://github.com/redhat-developer/rsp-server'
	}

	stage('Build') {
		def mvnHome = tool 'maven3-latest'
		env.PATH="${env.PATH}:${mvnHome}/bin"

		sh 'mvn clean install -Pintegration-tests'
		sh 'mvn clean package -f distribution/distribution/pom.xml'
		sh 'mvn clean compile exec:java package -f api/docs/org.jboss.tools.rsp.schema/pom.xml'

		archiveArtifacts artifacts: 'distribution/distribution/target/org.jboss.tools.rsp.distribution-*.zip,api/docs/org.jboss.tools.rsp.schema/target/*.jar'
	}
	
	stage('SonarCloud Report') {
		def mvnHome = tool 'maven3-latest'
		env.PATH="${env.PATH}:${mvnHome}/bin"

		sh 'mvn -P sonar sonar:sonar -Dsonar.login=${SONAR_TOKEN}'
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
