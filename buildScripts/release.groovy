def modules = ['microprofile','microprofile-bom','microprofile-config','microprofile-fault-tolerance',
	'microprofile-health','microprofile-jwt-auth','microprofile-metrics',
	'microprofile-open-api','microprofile-opentracing','microprofile-rest-client',
	'microprofile-reactive-streams', 'microprofile-reactive-messaging']
def moduleString = modules.join('\n')
pipeline {
    agent any
    tools {
        maven 'apache-maven-latest'
        jdk 'jdk1.8.0-latest'
    }
    parameters {
        string(description: 'The next snapshot version', name: 'snapshotVersion')
        string(description: 'The release version', name: 'releaseVersion')
        string(description: 'The SCM tag to apply', name: 'tag')
        choice(choices: 'Draft\nFinal', description: 'Revision Type', name: 'revremark')
        choice(choices: moduleString, description: 'Module', name: 'module')
        string(description: 'Branch to use', name: 'branch', defaultValue: 'master')
    }

    stages {
        stage("Checkout") {
            steps {
                git url: "git@github.com:eclipse/${params.module}.git", branch: params.branch
            }
        }
        stage("Execute Release") {
            steps {
                sh "mvn -s /opt/public/hipp/homes/genie.microprofile/.m2/settings-deploy-ossrh.xml release:prepare release:perform -B -Dtag=${params.tag} -DdevelopmentVersion=${params.snapshotVersion} -DreleaseVersion=${params.releaseVersion} -Drevremark=${params.revremark}"
            }
        }
        stage("Copy Specs") {
            steps {
                sh "mkdir -p /home/data/httpd/download.eclipse.org/microprofile/${params.module}-${params.releaseVersion}"
                sh "cp -r spec/target/generated-docs/* /home/data/httpd/download.eclipse.org/microprofile/${params.module}-${params.releaseVersion}"
                script {
                    if (fileExists('api')) {
                        sh "mkdir -p /home/data/httpd/download.eclipse.org/microprofile/${params.module}-${params.releaseVersion}/apidocs"
                        sh "cp -r api/target/apidocs/* /home/data/httpd/download.eclipse.org/microprofile/${params.module}-${params.releaseVersion}/apidocs"
                    }
                }
            }
        }
    }
    post {
        always {
            archive 'spec/target/generated-docs/*'
            deleteDir()
        }
    }
}
