@Library('funcoes-auxiliares') _

def pom = null
def tagVersion = null

pipeline {
  environment {
    WILDFLY_GROUP = 'main-server-group'
  }

  agent any

  tools {
    maven "M3"
  }

  options {
    // Only keep the 3 most recent builds
    buildDiscarder(logRotator(numToKeepStr:'3'))
    timeout(time: 10, unit: 'MINUTES')
  }

  stages{
    stage('CI') {
//       agent {
//         node { label 'maven'}
//       }
       stages{
         stage('Configurar Pipeline'){
           steps{
             script{
               def branch = "${env.BRANCH_NAME}"

               if (branch == 'master') {
                echo 'Env = Produção'
                env.ENVIRONMENT = "prd"
                env.REQUIRES_BUILD = 'N'
                env.REQUIRES_DEPLOYMENT = 'Y'
                env.REQUIRES_APPROVAL = 'Y'
                env.WILDFLY_URL = "${WILDFLY_URL_PRD}"
                env.PERFORM_RELEASE = 'N'

              } else if (branch.matches('^release/.+$')) {
                echo 'Env = HML'
                env.ENVIRONMENT = "hml"
                env.REQUIRES_BUILD = 'Y'
                env.REQUIRES_DEPLOYMENT = 'Y'
                env.REQUIRES_APPROVAL = 'Y'
                env.WILDFLY_URL = "${WILDFLY_URL_HML}"
                env.PERFORM_RELEASE = 'Y'

              } else if (branch.matches('^hotfix/.+$')) {
                echo 'Env = Hotfix'
                env.ENVIRONMENT = "hml"
                env.REQUIRES_BUILD = 'Y'
                env.REQUIRES_DEPLOYMENT = 'Y'
                env.REQUIRES_APPROVAL = 'Y'
                env.WILDFLY_URL = "${WILDFLY_URL_PRD}"
                env.PERFORM_RELEASE = 'Y'

              } else if (branch == 'develop') {
                echo 'Env = Desenvolvimento'
                env.ENVIRONMENT = "dev"
                env.REQUIRES_BUILD = 'Y'
                env.REQUIRES_DEPLOYMENT = 'Y'
                env.REQUIRES_APPROVAL = 'N'
                env.WILDFLY_URL = "${WILDFLY_URL_DEV}"
                env.PERFORM_RELEASE = 'N'
              } else if (branch.matches('^feature/.+$')) {
                echo 'Env = Feature'
                env.ENVIRONMENT = "dev"
                env.REQUIRES_BUILD = 'Y'
                env.REQUIRES_DEPLOYMENT = 'N'
                env.REQUIRES_APPROVAL = 'N'
                env.WILDFLY_URL = "${WILDFLY_URL_DEV}"
                env.PERFORM_RELEASE = 'N'
              } else {
                error "Erro de Validação: A branch ${branch} não é válida!"
              }
//              sh 'printenv'
             }
           }
         }
         stage('Checkout'){
           when{
             environment name: 'REQUIRES_BUILD', value: 'Y'
           }
           steps{
            checkout scm
           }
         }
         stage('Configurar Build') {
          when {
            environment name: 'REQUIRES_BUILD', value: 'Y'
          }
          steps {
            script {
              def branch = "${env.BRANCH_NAME}"
              pom = readMavenPom file: 'pom.xml'

              if (branch.matches('^release/.+$')) {
                env.VERSION = pom.version.replace("-SNAPSHOT","")
              } else if (branch.matches('^hotfix/.+$')) {
                echo 'Env = Hotfix'
                env.VERSION = branch.replaceAll('^hotfix/', '') + "-" + "${BUILD_ID}"
              } else if (branch == 'develop' || (branch.matches('^feature/.+$'))) {
                env.VERSION = getVersionFromPom()
              } else {
                error "Erro de Validação: A branch ${branch} não é válida!"
              }

              tagVersion = "${env.VERSION}"
              env.ARTIFACT_ID = getArtifactIdFromPom()
              env.GROUP_ID = getGroupIdFromPom()
            }
          }
        }
        stage('Build') {
          when {
            environment name: 'REQUIRES_BUILD', value: 'Y'
          }
          steps {
            script {
              echo "Excutando o build da aplicação..."
              withMaven(mavenSettingsConfig: 'maven-settings.xml',
	          options: [artifactsPublisher(disabled: true),findbugsPublisher(disabled: true),openTasksPublisher(disabled: true),junitPublisher(disabled: true),openTasksPublisher(disabled: true),jacocoPublisher(disabled: true)]) {
                sh "mvn compile -Ddependency-check.skip=true -Dmaven.test.skip=true"
              }
            }
          }
        }
        stage('Testes Unitários') {
          when {
            environment name: 'REQUIRES_BUILD', value: 'Y'
          }
          steps {
            script {
              echo "Executando testes unitários..."
              withMaven(mavenSettingsConfig: 'maven-settings.xml',
	          options: [artifactsPublisher(disabled: true),findbugsPublisher(disabled: true),openTasksPublisher(disabled: true),junitPublisher(disabled: true),openTasksPublisher(disabled: true),jacocoPublisher(disabled: true)]) {
                sh "mvn test -Ddependency-check.skip=true"
              }
            }
          }
        }
        stage('Testes de Integração') {
          when {
            environment name: 'REQUIRES_BUILD', value: 'Y'
          }
          steps {
            script {
              echo "Executando testes unitários..."
              withMaven(mavenSettingsConfig: 'maven-settings.xml',
	          options: [artifactsPublisher(disabled: true),findbugsPublisher(disabled: false),openTasksPublisher(disabled: false),junitPublisher(disabled: false),openTasksPublisher(disabled: true),jacocoPublisher(disabled: false)]) {
                sh "mvn clean verify"
              }
            }
          }
        }
        stage('Analise Estática SonarQube') {
          when {
            environment name: 'REQUIRES_BUILD', value: 'Y'
          }
          steps {
            script {
              echo "Executando análise estática..."
            	pom = readMavenPom file: "pom.xml";
            	echo "GroupId: ${GROUP_ID} ArtifactId: ${ARTIFACT_ID} Version: ${VERSION}"

                withMaven(mavenSettingsConfig: 'maven-settings.xml',
    	          options: [artifactsPublisher(disabled: true),findbugsPublisher(disabled: true),openTasksPublisher(disabled: true),junitPublisher(disabled: true),openTasksPublisher(disabled: true),jacocoPublisher(disabled: true)]) {
                  withSonarQubeEnv('SonarQube-7.9.2') {
                    sh "mvn sonar:sonar -Dsonar.projectName=${ARTIFACT_ID}-${env.BRANCH_NAME} -Dsonar.projectKey=${GROUP_ID}-${ARTIFACT_ID}-${env.BRANCH_NAME} -Dsonar.projectVersion=$BUILD_NUMBER -Dsonar.dependencyCheck.reportPath=target/dependency-check-report.xml -Dsonar.dependencyCheck.htmlReportPath=target/dependency-check-report.html"
                  }
                }

                timeout(time: 2, unit: 'MINUTES') {
                  withSonarQubeEnv('SonarQube-7.9.2') {
                  	def qg = waitQualityGate()
                  	echo "Resultado da análise completo: ${qg}"

                  	if (qg.status == 'ERROR') {
                    	error "Falha devido a má qualidade do código.\nStatus da análise: ${qg.status}"
                  	}
                  	echo "Status da análise: ${qg.status}"
                  }
                }

            	}
            }
            post {
	          success {
	              sh 'tar -czvf target/dependency-check-reports.tar.gz target/dependency-check-report.*'
	              archiveArtifacts artifacts: 'target/dependency-check-reports.tar.gz', onlyIfSuccessful: true

	              archiveArtifacts artifacts: '**/jacoco.exec', onlyIfSuccessful: true

	              sh 'tar -czvf target/surefire-reports.tar.gz target/surefire-reports/*.*'
	              archiveArtifacts artifacts: 'target/surefire-reports.tar.gz', onlyIfSuccessful: true

	              sh 'tar -czvf target/sonar.tar.gz target/sonar'
	              archiveArtifacts artifacts: 'target/sonar.tar.gz', onlyIfSuccessful: true

	              sh 'tar -czvf target/jacoco.tar.gz target/site/jacoco'
	              archiveArtifacts artifacts: 'target/jacoco.tar.gz', onlyIfSuccessful: true

	          }
	      	}
        }
        stage('Atualizando Release') {
          when {
            environment name: 'PERFORM_RELEASE', value: 'Y'
          }
          steps {
            script {

                withCredentials([usernamePassword(credentialsId: 'jenkins-user-pass', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                  sh 'git config user.email $PASSWORD'
                  sh 'git config user.name $USERNAME'
                }

                withMaven(mavenSettingsConfig: 'maven-settings.xml',options: [artifactsPublisher(disabled: true),findbugsPublisher(disabled: true),openTasksPublisher(disabled: true),junitPublisher(disabled: true),openTasksPublisher(disabled: true),jacocoPublisher(disabled: true)]) {
                  sh "clean release:clean release:prepare release:perform -Dmaven.test.skip=true"
                }
            }
          }
        }
        stage('Publicar no Nexus') {
          when {
            allOf{
              environment name: 'REQUIRES_BUILD', value: 'Y'
            }
          }
          steps {
            script {
              echo "Exportando para o nexus..."
                withMaven(mavenSettingsConfig: 'maven-settings.xml',options: [artifactsPublisher(disabled: true),findbugsPublisher(disabled: true),openTasksPublisher(disabled: true),junitPublisher(disabled: true),openTasksPublisher(disabled: true),jacocoPublisher(disabled: true)]) {
                    sh "mvn deploy -Dmaven.test.skip=true -Ddependency-check.skip=true"
                  }
            }
          }
        }
       }
    }//end CI
    stage("CD") {
      stages {
        stage('Realizar Deploy do artefato') {
          when {
            allOf {
              environment name: 'REQUIRES_DEPLOYMENT', value: 'Y'
            }
          }
          agent "any"
          steps {
            script {
              echo "Realizando deploy no ambiente  ${ENVIRONMENT}, Host ${WILDFLY_URL}"
              withMaven(mavenSettingsConfig: 'maven-settings.xml',options: [artifactsPublisher(disabled: true),findbugsPublisher(disabled: true),openTasksPublisher(disabled: true),junitPublisher(disabled: true),openTasksPublisher(disabled: true),jacocoPublisher(disabled: true)]) {
                  sh "mvn wildfly:undeploy -Pwildfly-domain -Dmaven.test.skip=true -Ddependency-check.skip=true"
                  sh "mvn wildfly:execute-commands -Pwildfly-domain -Dmaven.test.skip=true -Ddependency-check.skip=true"
                  sh "mvn wildfly:deploy -Pwildfly-domain -Dmaven.test.skip=true -Ddependency-check.skip=true"
                }
            }
          }
        }
      }//stages
    }//end CD
  }

}
