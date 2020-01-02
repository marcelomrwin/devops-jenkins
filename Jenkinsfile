@Library('funcoes-auxiliares') _

def version = null
def artifactId = null
def groupId = null
def pom = null

pipeline {
  environment {
    APPROVERS_PRD_GROUP = 'approvers_prd'
    APPROVERS_HML_GROUP = 'approvers_hml'
    OPERATIONS_GROUP = 'operations'
  }

  agent any

  tools {
    maven "M3"
  }

  options {
    // Only keep the 3 most recent builds
    buildDiscarder(logRotator(numToKeepStr:'3'))
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
                env.REQUIRES_PRD_APPROVAL = 'Y'

              } else if (branch.matches('^release/.+$')) {
                echo 'Env = HML'
                env.ENVIRONMENT = "hml"
                env.REQUIRES_BUILD = 'Y'
                env.REQUIRES_DEPLOYMENT = 'Y'
                env.REQUIRES_APPROVAL = 'Y'
                env.REQUIRES_HML_APPROVAL = 'Y'

              } else if (branch.matches('^hotfix/.+$')) {
                echo 'Env = Hotfix'
                env.ENVIRONMENT = "hml"
                env.REQUIRES_BUILD = 'Y'
                env.REQUIRES_DEPLOYMENT = 'Y'
                env.REQUIRES_APPROVAL = 'Y'
                env.REQUIRES_HML_APPROVAL = 'Y'

              } else if (branch == 'develop') {
                echo 'Env = Desenvolvimento'
                env.ENVIRONMENT = "dev"
                env.REQUIRES_BUILD = 'Y'
                env.REQUIRES_DEPLOYMENT = 'Y'
                env.REQUIRES_APPROVAL = 'N'
              } else if (branch.matches('^feature/.+$')) {
                echo 'Env = Feature'
                env.ENVIRONMENT = "dev"
                env.REQUIRES_BUILD = 'Y'
                env.REQUIRES_DEPLOYMENT = 'N'
                env.REQUIRES_APPROVAL = 'N'
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

              if (branch.matches('^release/.+$')) {
              	pom = readMavenPom file: 'pom.xml'
                env.VERSION = pom.version.replace("-SNAPSHOT","")
              } else if (branch.matches('^hotfix/.+$')) {
                echo 'Env = Hotfix'
                env.VERSION = branch.replaceAll('^hotfix/', '') + "-" + "${BUILD_ID}"
              } else if (branch == 'develop') {
                env.VERSION = getVersionFromPom() + "-" + "${BUILD_ID}"
              } else {
                error "Erro de Validação: A branch ${branch} não é válida!"
              }

              env.ARTIFACT_ID = getArtifactIdFromPom()              
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
              withMaven(mavenSettingsConfig: 'maven-settings.xml') {
                sh "mvn compile"
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
              withMaven(mavenSettingsConfig: 'maven-settings.xml') {
                sh "mvn test"
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
			  	version = getVersionFromPom()
            	groupId = getGroupIdFromPom()
            	artifactId = getArtifactIdFromPom()
            	pom = readMavenPom file: "pom.xml";
            	
            	echo "GroupId: ${groupId} ArtifactId: ${artifactId} Version: ${version}"
			  
                withMaven(mavenSettingsConfig: 'maven-settings.xml',
	          options: [
	            artifactsPublisher(disabled: true),
	            findbugsPublisher(disabled: false),
	            openTasksPublisher(disabled: false),
	            junitPublisher(disabled: false)
	          ]) {
                  withSonarQubeEnv('SonarQube-7.9.2') {
                    sh "mvn sonar:sonar -Dsonar.projectName=${groupId}:${artifactId} -Dsonar.projectKey=${groupId}:${artifactId} -Dsonar.projectVersion=$BUILD_NUMBER"
                  }
                }

                timeout(time: 2, unit: 'MINUTES') {
                  def qg = waitQualityGate()
                  
                  if (qg.status != 'OK') {
                    error "Falha devido a má qualidade do código.\nStatus da análise: ${qg.status}"
                  }
                  echo "Status da análise: ${qg.status}"
                }
              
            	}
            }
            post {
	          success {
	              archiveArtifacts artifacts: '**/dependency-check-report.json', onlyIfSuccessful: true
	              archiveArtifacts artifacts: '**/jacoco.exec', onlyIfSuccessful: true
	              sh 'tar -czvf target/sonar.tar.gz target/sonar'
	              archiveArtifacts artifacts: 'target/sonar.tar.gz', onlyIfSuccessful: true
	
	              sh 'tar -czvf target/jacoco.tar.gz target/site/jacoco'
	              archiveArtifacts artifacts: 'target/jacoco.tar.gz', onlyIfSuccessful: true
	
	              sh 'tar -czvf target/cobertura.tar.gz target/site/cobertura'
	              archiveArtifacts artifacts: 'target/cobertura.tar.gz', onlyIfSuccessful: true
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
              withMaven(mavenSettingsConfig: 'maven-settings.xml') {
                sh "mvn clean verify"
              }              
            }
          }
        }
       }
    }

  }

}
