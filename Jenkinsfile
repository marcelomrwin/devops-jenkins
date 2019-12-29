node {
  def mvnHome
  def pom
  def artifactVersion
  def tagVersion
  def retrieveArtifact

	environment {
        GITHUB_CREDENTIALS = credentials('github-credential')
    }

  stage('Prepare') {
    mvnHome = tool 'M2'
  }

  stage('Checkout') {
     checkout scm
  }

  stage('Build') {
     if (isUnix()) {
        sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean package"
     } else {
        bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore clean package/)
     }
  }

  stage('Unit Test') {
     junit '**/target/surefire-reports/TEST-*.xml'
     archive 'target/*.jar'
  }

  stage('Integration Test') {
    if (isUnix()) {
       sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean verify"
    } else {
       bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore clean verify/)
    }
  }

  stage('Sonar') {
     if (isUnix()) {
        sh "'${mvnHome}/bin/mvn' sonar:sonar -Psonar"
     } else {
        bat(/"${mvnHome}\bin\mvn" sonar:sonar -Psonar/)
     }
  }

  if(env.BRANCH_NAME == 'master'){
    stage('Validate Build Post Prod Release') {
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' clean package"
      } else {
         bat(/"${mvnHome}\bin\mvn" clean package/)
      }
    }

  }

  if(env.BRANCH_NAME == 'develop'){
    stage('Snapshot Build And Upload Artifacts') {
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' clean deploy"
      } else {
         bat(/"${mvnHome}\bin\mvn" clean deploy/)
      }
    }

    stage('Deploy') {
       sh 'curl -u jenkins:jenkins -T target/**.war "http://192.168.1.116:8888/manager/text/deploy?path=/devops&update=true"'
    }

    stage("Smoke Test"){
       sh "curl --retry-delay 10 --retry 5 http://192.168.1.116:8888/devops"
    }    
  }
  
   if (env.BRANCH_NAME ==~ /release.*/){
	pom = readMavenPom file: 'pom.xml'
	artifactVersion = pom.version.replace("-SNAPSHOT","")
	tagVersion = artifactVersion
	
	stage('Configure GitHub Credentials'){
	      sh '''
	      git config user.email "$GITHUB_CREDENTIALS_PSW"
	      git config user.name "$GITHUB_CREDENTIALS_USR"
	      '''               
	}
	
	stage('Release Build And Upload Artifacts'){
		if (isUnix()){
		    sh "'${mvnHome}/bin/mvn' clean release:clean release:prepare release:perform"
		}else{
		    bat(/"${mvnHome}\bin\mvn" clean release:clean release:prepare release:perform/)
		}
	}
	
	stage('Deploy to Dev'){
	    sh 'curl -u jenkins:password -T target/**.war "http://192.168.1.116:8888/manager/text/deploy?path=/devops&update=true"'
	}
	
	stage("Smoke Test Dev"){
       sh "curl --retry-delay 10 --retry 5 http://192.168.1.116:8888/devops"
    }
    
    stage("QA Approval"){
        echo "Job '${env.JOB_NAME}' (${env.BUILD_NUMBER}) is waiting for input. Please go to ${env.BUILD_URL}."
        input 'Approval for QA Deploy?';
    }

	stage("Deploy from Artifactory to QA"){
		retrieveArtifact = 'http://192.168.1.116:8081/artifactory/libs-release-local/com/redhat/devops/' + artifactVersion + '/devops-' + artifactVersion + '.war'
		echo "${tagVersion} with artifact version ${artifactVersion}"
		echo "Deploying war from http://192.168.1.116:8081/artifactory/libs-release-local/com/redhat/devops/${artifactVersion}/devops-${artifactVersion}.war"
		sh 'curl -O ' + retrieveArtifact
		sh 'curl -u jenkins:password -T target/**.war "http://192.168.1.116:8898/manager/text/deploy?path=/devops&update=true"'                                   
	}
	
	stage("Smoke Test QA"){
       sh "curl --retry-delay 10 --retry 5 http://192.168.1.116:8898/devops"
    }                      
 }

}