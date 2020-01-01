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
               sh 'printenv'
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
              sh 'printenv'
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
       }
    }

  }

}
