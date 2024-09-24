pipeline {
    agent any

    options {
        disableConcurrentBuilds(abortPrevious: true)
        timeout(activity: true, time: 10, unit: 'MINUTES')
    }

    tools {
        maven 'DEFAULT'
        jdk 'JDK 11 Adopt'
    }

    parameters {
        booleanParam(
                name: 'skipTests',
                description: 'Skip tests',
                defaultValue: false
        )
        booleanParam(
                name: 'RELEASE_FLAG',
                description: 'Select to publish new version'
        )
        string(
                name: 'RELEASE_VERSION',
                description: 'Version that it will be released under',
                trim: true
        )
        string(
                name: 'NEW_VERSION',
                description: 'Version that project will now have set',
                trim: true
        )
    }

    stages {
        stage('Build') {
            steps {
                script {
                    sh """
                        mvn -B install
                        """
                }
            }
        }

        stage('Check Release parameters') {
            when {
                expression { params.RELEASE_FLAG }
            }
            steps {
                script {
                    if (!params.RELEASE_VERSION) {
                        error('RELEASE_VERSION not set')
                    }
                    if (!params.NEW_VERSION) {
                        error('NEW_VERSION not set')
                    }
                }
            }
        }

        stage('Release') {
            when {
                expression { params.RELEASE_FLAG }
            }
            steps {
                script {
                    releaseVersion = params.RELEASE_VERSION
                    newVersion = params.NEW_VERSION
                    sh """
                        mvn versions:set -DnewVersion=${releaseVersion}

                        git config user.email "jenkins-ci@psi.pl"
                        git config user.name "Jenkins"
                        git add pom.xml
                        git commit -am "Release ${releaseVersion}"
                        git tag -a -m "Jenkins automated release" ${releaseVersion}
                        mvn clean deploy -P TPF

                        mvn versions:set -DnewVersion=${newVersion}
                        git add pom.xml
                        git commit -am "Prepare for next development iteration: ${newVersion}"
                        """
                    withCredentials(
                            [gitUsernamePassword(credentialsId: 'ad-service-user-psi', gitToolName: 'DEFAULT')]
                    ) {
                        sh """
                            git push origin tag ${releaseVersion}
                            git push origin HEAD:${env.BRANCH_NAME}
                        """
                    }

                }
            }

        }
    }
}