pipeline {
    agent any
    triggers {
        pollSCM 'H 6 * * *'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '100', artifactNumToKeepStr: '1'))
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        skipDefaultCheckout true
    }
    tools {
        jdk 'openjdk-1.8'
        ant 'apache-ant-1.10.7'
    }
    stages {
        stage('checkout') {
            steps {
                checkout([$class: 'SubversionSCM',
                    locations: [[remote: 'http://wetator.repositoryhosting.com/svn_public/wetator_wetator/trunk/wetator-ant-task', local: '.', depthOption: 'infinity', ignoreExternalsOption: true, cancelProcessOnExternalsFail: true]],
                    quietOperation: true,
                    workspaceUpdater: [$class: 'CheckoutUpdater']])
            }
        }
        stage('build') {
            steps {
                wrap([$class: 'Xvfb']) {
                    sh "ant publish-local"
                }
            }
        }
    }
    post {
        always {
            junit allowEmptyResults: true, testResults: 'deploy/junit/*.xml'
            recordIssues enabledForFailure: true, sourceCodeEncoding: 'UTF-8', sourceDirectory: 'src', tools: [
                checkStyle(pattern: 'deploy/checkstyle/checkstyle-report.xml', reportEncoding: 'UTF-8'),
                spotBugs(pattern: 'deploy/spotbugs/spotbugs-report.xml', reportEncoding: 'UTF-8', useRankAsPriority: true),
                pmdParser(pattern: 'deploy/pmd/pmd-report.xml', reportEncoding: 'UTF-8'),
                cpd(pattern: 'deploy/pmd/cpd-report.xml', reportEncoding: 'UTF-8'),
                java(),
                javaDoc(),
                taskScanner(includePattern: '**/*.java, **/*.xhtml, **/*.jsp, **/*.html, **/*.js, **/*.css, **/*.xml, **/*.wet, **/*.properties', highTags: 'FIXME, XXX', normalTags: 'TODO')]
            archiveArtifacts artifacts: 'deploy/wetator-*.zip, deploy/wetator-*.jar', allowEmptyArchive: true, fingerprint: true
            step([$class: 'Mailer',
                notifyEveryUnstableBuild: true,
                recipients: "rbri@rbri.de, frank.danek@gmail.com, tobias.woerenkaemper@gmx.com"])
        }
    }
}