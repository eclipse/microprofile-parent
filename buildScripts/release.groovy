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
        choice(choices: 'microprofile-rest-client\nmicroprofile-fault-tolerance\nmicroprofile-metrics\nmicroprofile-open-api\nmicroprofile-opentracing\nmicroprofile-config\nmicroprofile-bom', description: 'Module', name: 'module')
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
                sh "mkdir -p /home/data/httpd/download.eclipse.org/microprofile/${params.module}-${params.releaseVersion}/apidocs"
                sh "cp -r spec/target/generated-docs/* /home/data/httpd/download.eclipse.org/microprofile/${params.module}-${params.releaseVersion}"
                sh "cp -r api/target/apidocs/* /home/data/httpd/download.eclipse.org/microprofile/${params.module}-${params.releaseVersion}/apidocs"
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
