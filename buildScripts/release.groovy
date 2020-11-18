def modules = ['microprofile','microprofile-bom','microprofile-config','microprofile-context-propagation','microprofile-fault-tolerance',
	'microprofile-health','microprofile-jwt-auth','microprofile-metrics',
	'microprofile-open-api','microprofile-opentracing','microprofile-rest-client',
	'microprofile-reactive-streams-operators', 'microprofile-reactive-messaging', 'microprofile-lra', 'microprofile-graphql']
def moduleString = modules.join('\n')
pipeline {
    agent any
    tools {
        maven 'apache-maven-latest'
        jdk 'adoptopenjdk-hotspot-jdk8-latest'
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
                git credentialsId: 'github-bot-ssh', url: "git@github.com:eclipse/${params.module}.git", branch: params.branch
            }
        }
        stage("Execute Release") {
            steps {
                withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
                    sh 'gpg --batch --import "${KEYRING}"'
                    sh 'for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
                }
                sshagent(['github-bot-ssh']) {
                    sh '''
                        git config --global user.email "microprofile-bot@eclipse.org"
                        git config --global user.name "Eclipse MicroProfile bot"
                    '''
                    sh "mvn -s /home/jenkins/.m2/settings.xml release:prepare release:perform -B -Dtag=${params.tag} -DdevelopmentVersion=${params.snapshotVersion} -DreleaseVersion=${params.releaseVersion} -Drevremark=${params.revremark}"
                }
            }
        }
        stage("Copy Specs") {
            steps {
                sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
                    sh "ssh genie.microprofile@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/microprofile/staging/${params.module}-${params.releaseVersion}"
                    sh "scp -r spec/target/generated-docs/* genie.microprofile@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/microprofile/staging/${params.module}-${params.releaseVersion}"
                    script {
                        if (fileExists('api')) {
                            sh "scp api/target/*.jar genie.microprofile@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/microprofile/staging/${params.module}-${params.releaseVersion}/"

                            sh "ssh genie.microprofile@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/microprofile/staging/${params.module}-${params.releaseVersion}/apidocs"
                            sh "scp -r api/target/apidocs/* genie.microprofile@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/microprofile/staging/${params.module}-${params.releaseVersion}/apidocs"
                        }
                        if (fileExists('tck')) {
                            sh "scp tck/target/*.jar genie.microprofile@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/microprofile/staging/${params.module}-${params.releaseVersion}/"
                        }
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
