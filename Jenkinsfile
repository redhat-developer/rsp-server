#!/usr/bin/env groovy
 
def default_java = "openjdk-11"
def default_os = "rhel7"
def java_axes = []
def os_axes = []
if (params.SIMPLE_MATRIX) {
    java_axes = ["openjdk-11"]
    os_axes = ["rhel7"]
} else {
    java_axes = ["openjdk-11"]
    os_axes = ["rhel7", "win10"]
}

def axes = [:]
os_axes.each {
    os -> java_axes.each {
        java ->
            if (!(os.equalsIgnoreCase(default_os) && java.equalsIgnoreCase(default_java))) {
                axes.put(os + '-' + java, stageClosure(os, java))
            }
    }
}

def runCommand(String command) {
    if (isUnix()) {
        sh "${command}"
    } else {
        bat "${command}"
    }
}

def stageClosure(String os_label, String java) {
    return {
        node(os_label) {
            def description = "${os_label} ${java}"
            stage("${description}") {
                try {
                    withEnv(["JAVA_HOME=${tool java}", "MAVEN=${tool 'maven3-latest'}/bin"]) {
                        deleteDir()
                        unstash 'source'
                        runCommand("${MAVEN}/mvn clean verify -fae -B -Pintegration-tests -DskipTests=true -Dmaven.test.failure.ignore=true")
                    }
                } catch (e) {
                    echo "${e.getMessage()}"
                    // Since we're catching the exception in order to report on it,
                    // we need to re-throw it, to ensure that the build is marked as failed
                    throw e
                } finally {
                    junit '**/surefire-reports/*.xml'
                    stash includes: '**/surefire-reports/*.xml,**/integration-tests/target/surefire-reports/*,**/tests/**/target/surefire-reports/*', name: "${os_label}-${java}"
                }
            }
        }
    }
}

pipeline {

    agent {
        label 'rhel7-micro'
    }

    options {
        timeout(time: 2, unit: 'HOURS')
    }

    tools {
        jdk 'openjdk-11'
    }
    
    stages {
        stage('Checkout SCM') {
            steps {
                deleteDir()
                git url: "https://github.com/${params.FORK}/rsp-server.git", branch: params.BRANCH
                stash includes: '**', name: 'source'
            }
        }
        stage('Build and tests') {
            parallel {
                stage('Java 11 runtime') {
                    agent {
                        label 'rhel7'
                    }
                    stages {
                        stage('Build & unit tests') {
                            steps {
                                unstash 'source'
                                sh 'mvn clean install -fae -B'
                                archiveArtifacts artifacts: 'distribution/distribution*/target/org.jboss.tools.rsp.distribution*.zip,api/docs/org.jboss.tools.rsp.schema/target/*.jar,site/target/repository/**', allowEmptyArchive: true
                                stash includes: 'distribution/distribution*/target/org.jboss.tools.rsp.distribution*.zip,api/docs/org.jboss.tools.rsp.schema/target/*.jar', name: 'zips'
                                stash includes: 'site/target/repository/**', name: 'site'
                            }
                        }

                       stage('Integration tests') {
                           steps {
                               sh 'mvn verify -B -Pintegration-tests -DskipTests=true -Dmaven.test.failure.ignore=true'
                               archiveArtifacts artifacts: 'distribution/integration-tests/target/quickstarts/*/build.log', allowEmptyArchive: true
                           }
                       }

                        stage('SonarCloud Report') {
                            when {
                                expression {
                                    params.SONAR
                                }
                            }
                            steps {
                                script {
                                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                                        sh '''
                                            set +x
                                            mvn -B -P sonar sonar:sonar -Dsonar.login="${SONAR_TOKEN}"
                                        '''
                                    }
                                }
                            }
                        }
                    }
                    post {
                        always {
                            junit '**/surefire-reports/*.xml'
                            archiveArtifacts artifacts: '**/integration-tests/target/surefire-reports/*,**/tests/**/target/surefire-reports/*', allowEmptyArchive: true
                        }
                    }
                }

               stage('Parallel integration tests') {
                   steps {
                       script {
                           parallel axes
                       }
                   }
               }
            }
        }
    }
    post {
        always {
            script {
                axes.each {
                    dir("${it.key}") {
                        unstash "${it.key}"
                    }
                    archiveArtifacts artifacts: "${it.key}/**/integration-tests/target/surefire-reports/*,**/tests/**/target/surefire-reports/*", allowEmptyArchive: true
                }
            }
        }
        success {
            script {
                unstash 'site'
                unstash 'zips'

                // choose remote upload directory
                def upload_dir = params.publish ? 'stable' : 'snapshots'

                // get distribution version
                def distroVersion = sh script: "ls distribution/distribution/target/*.zip | cut --complement -f 1 -d '-' | rev | cut -c5- | rev | tr -d '\n'", returnStdout: true

                // First empty the remote dirs
                def emptyDir = sh script: "mktemp -d | tr -d '\n'", returnStdout: true
                sh "chmod 775 ${emptyDir}"
                sh "rsync -Pzrlt -e 'ssh -p 2222' --protocol=28 --delete ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server/p2/${distroVersion}/"
                sh "rsync -Pzrlt -e 'ssh -p 2222' --protocol=28 --delete ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server/p2/${distroVersion}/plugins/"

                // Upload the p2 update site.  This logic only works because all plugins are jars. 
                // If we ever have exploded bundles here, this will need to be redone
                def siteRepositoryFilesToPush = findFiles(glob: 'site/target/repository/*')
                def sitePluginFilesToPush = findFiles(glob: 'site/target/repository/plugins/*')
                for (i = 0; i < siteRepositoryFilesToPush.length; i++) {
                    sh "rsync -Pzrlt -e 'ssh -p 2222' --protocol=28 ${siteRepositoryFilesToPush[i].path} ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server/p2/${distroVersion}/"
                }
                for (i = 0; i < sitePluginFilesToPush.length; i++) {
                    sh "rsync -Pzrlt -e 'ssh -p 2222' --protocol=28 ${sitePluginFilesToPush[i].path} ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server/p2/${distroVersion}/plugins/"
                }

                // find rsp distribution zip and create symlink with word latest in the file name
                dir('distribution/distribution/target') {
                    sh "cp org.jboss.tools.rsp.distribution-${distroVersion}.zip org.jboss.tools.rsp.distribution-latest.zip"
                }
                // Upload distributions / zips
                def filesToPush = findFiles(glob: '**/*.zip')
                for (i = 0; i < filesToPush.length; i++) {
                    sh "rsync -Pzrlt -e 'ssh -p 2222' --protocol=28 ${filesToPush[i].path} ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server/"
                }

        		sh "echo org.jboss.tools.rsp.distribution.latest.version=${distroVersion} > LATEST"
        		sh "echo org.jboss.tools.rsp.distribution.latest.url=https://download.jboss.org/jbosstools/adapters/${upload_dir}/rsp-server/org.jboss.tools.rsp.distribution-${distroVersion}.zip >> LATEST"
        		sh "rsync -Pzrlt -e 'ssh -p 2222' --protocol=28 LATEST ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server/"
            }
        }
    }
}
