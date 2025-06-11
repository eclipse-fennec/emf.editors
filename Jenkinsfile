pipeline  {
    agent any

    tools {
        jdk 'OpenJDK17'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    stages {
        stage('Prepare Gradle') {
          steps {
            echo "I am building app on branch: ${env.GIT_BRANCH}"
            sh "chmod +x ./gradlew"
          }
        }
        
        stage('Main branch release') {
            when { 
                branch 'main' 
            }
            steps {
                echo "I am building on ${env.BRANCH_NAME}"
                sh "./gradlew clean build --info --stacktrace -Dmaven.repo.local=${WORKSPACE}/.m2"
            }
        }
        stage('Snapshot branch release') {
            when { 
                branch 'snapshot'
            }
            steps  {
                echo "I am building on ${env.JOB_NAME}"
                sh "./gradlew clean build --info --stacktrace -Dmaven.repo.local=${WORKSPACE}/.m2"
            }
        }
        stage('Archive Results') {
            steps {
                archiveArtifacts 'org.eclipse.fennec.emf.editor.reflective.xmi/generated/p2updatesite.zip'
            }
        }
    }
}
