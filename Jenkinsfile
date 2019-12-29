node {
	def mvnHome
	stage('Prepare'){
		git 'https://github.com/marcelomrwin/devops-jenkins.git'
		mvnHome = tool 'M2'
	}
	
	stage('Build'){
		sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean package"
	}
	
	stage('Unit Test'){
		junit '**/target/surefire-reports/TEST-*.xml'
		archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
	}
	
	stage('Integration Test'){
		sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean verify"
	}
	
	stage('Sonar'){
		sh "'${mvnHome}/bin/mvn' sonar:sonar -Psonar"
	}
	
	stage('Deploy'){
		sh 'curl -u admin:password -T target/**.war "http://192.168.1.116:8888/manager/text/deploy?path=/devops&update=true"'
	}
	
	stage('Smoke Test'){
		sh 'curl --retry-delay 10 --retry 5 http://192.168.1.116:8888/devops'
	}

}
