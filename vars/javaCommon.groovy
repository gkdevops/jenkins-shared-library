def call(String repoUrl) {
  pipeline {
    
    agent {
        label 'agent_1'
    }
    
    tools {
          maven 'MAVEN3'
          jdk 'JDK8'
    }
    
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')
        disableConcurrentBuilds()
        timestamps()
    }
    
    stages {
        stage('Code Checkout'){
            steps {
                echo "code checkout"
                git credentialsId: 'github-creds', url: 'https://github.com/gkdevops/"{appName}".git'
            }
        }
        
        stage('Code Build'){
            steps {
                sh "mvn test-compile"
            }
        }
                stage('Unit test') {
                    steps {
                        sh "mvn test"
                    }
                }
        stage('SonarQube Scan'){
          environment {
            SCANNER_HOME = tool 'sonar_scanner'
          }
          steps {
            withSonarQubeEnv (installationName: 'SonarQube') {
              sh "${SCANNER_HOME}/bin/sonar-scanner -Dproject.settings=sonar-project.properties"
            }
          }
        }
        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Code Package'){
            steps {
                sh "mvn package"
            }
        }
    }
  }
}
