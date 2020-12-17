def modules = ['microprofile','microprofile-bom','microprofile-config','microprofile-context-propagation','microprofile-fault-tolerance',
	'microprofile-health','microprofile-jwt-auth','microprofile-metrics',
	'microprofile-open-api','microprofile-opentracing','microprofile-rest-client',
	'microprofile-reactive-streams-operators', 'microprofile-reactive-messaging', 'microprofile-lra', 'microprofile-graphql']
def moduleString = modules.join('\n')
pipeline {
    agent any
    parameters {
        string(description: 'The release version', name: 'releaseVersion')
        choice(choices: moduleString, description: 'Module', name: 'module')
    }

    stages {
        stage("Move Specs From Staging") {
            steps {
                sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
                    sh "ssh genie.microprofile@projects-storage.eclipse.org [ -e /home/data/httpd/download.eclipse.org/microprofile/staging/${params.module}-${params.releaseVersion} ] || (echo 'The requested module ${params.module}-${params.releaseVersion} not found in microprofile/staging/ directory' && exit 1)"
                    sh "ssh genie.microprofile@projects-storage.eclipse.org mv /home/data/httpd/download.eclipse.org/microprofile/staging/${params.module}-${params.releaseVersion} /home/data/httpd/download.eclipse.org/microprofile/"
                }
            }
        }
    }
}
