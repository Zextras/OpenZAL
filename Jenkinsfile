def mvnCmd(String cmd) {
  sh 'mvn -B -Djooq.codegen.logging=WARN -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn ' + cmd
}

def supportedVersions() {
    return ["22.8.0", "22.10.0"]
}

def buildForAllSupportedVersions() {
    supportedVersions().eachWithIndex { version, idx ->
      mvnCmd("--settings settings-jenkins.xml -Dzimbra.version=$version package")
    }

    // dev version
    mvnCmd("--settings settings-jenkins.xml package -Pdev")

    // latest with no classifier
    mvnCmd("--settings settings-jenkins.xml package")
}

def deployForAllSupportedVersions() {
    supportedVersions().eachWithIndex { version, idx ->
      mvnCmd("--settings settings-jenkins.xml -Dzimbra.version=$version deploy")
    }

    // dev version
    mvnCmd("--settings settings-jenkins.xml deploy -Pdev")

    // latest with no classifier
    mvnCmd("--settings settings-jenkins.xml deploy")
}

pipeline {
    agent {
        node {
            label 'zextras-agent-v3'
        }
    }
    parameters {
        booleanParam defaultValue: false, description: 'Whether to upload the packages in playground repositories', name: 'PLAYGROUND'
    }
    environment {
        JAVA_OPTS="-Dfile.encoding=UTF8"
        LC_ALL="C.UTF-8"
        jenkins_build="true"
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        timeout(time: 2, unit: 'HOURS')
        skipDefaultCheckout()
    }
    stages {
        stage('Setup') {
            steps {
                checkout scm
                withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
                    sh "cp ${SETTINGS_PATH} settings-jenkins.xml"
                }
            }
        }
        stage('Build') {
            steps {
                buildForAllSupportedVersions()
            }
        }
        stage('Publish dev version') {
            when {
                branch 'main'
            }
            steps {
                mvnCmd("--settings settings-jenkins.xml deploy -Pdev")
            }
        }
        stage('Publish tagged version') {
            when {
                anyOf {
                    buildingTag()
                }
            }
            steps {
                deployForAllSupportedVersions()
            }
        }
        stage('Build deb/rpm') {
          when {
              anyOf {
                  branch 'release/*'
                  branch 'custom/*'
                  branch 'beta/*'
                  branch 'playground/*'
                  branch 'main'
                  expression { params.PLAYGROUND == true }
                  buildingTag()
              }
          }
          stages {
              stage('Stash') {
                  steps {
                      sh 'cp target/zal-*-zimbra-*.jar packages/'
                      stash includes: "pacur.json,packages/**", name: 'binaries'
                  }
              }
              stage('pacur') {
                  parallel {
                      stage('Ubuntu 18.04') {
                          agent {
                              node {
                                  label 'pacur-agent-ubuntu-18.04-v1'
                              }
                          }
                          steps {
                              unstash 'binaries'
                              sh 'sudo pacur build ubuntu'
                              stash includes: 'artifacts/', name: 'artifacts-deb'
                          }
                          post {
                              always {
                                  archiveArtifacts artifacts: "artifacts/*.deb", fingerprint: true
                              }
                          }
                      }

                      stage('Rocky 8') {
                          agent {
                              node {
                                  label 'pacur-agent-rocky-8-v1'
                              }
                          }
                          steps {
                              unstash 'binaries'
                              sh 'sudo pacur build centos'
                              stash includes: 'artifacts/', name: 'artifacts-rpm'
                          }
                          post {
                              always {
                                  archiveArtifacts artifacts: "artifacts/*.rpm", fingerprint: true
                              }
                          }
                      }
                  }
              }
          }
      }
      stage('Upload To Playground') {
          when {
              anyOf {
                  branch 'playground/*'
                  expression { params.PLAYGROUND == true }
              }
          }
          steps {
              unstash 'artifacts-deb'
              unstash 'artifacts-rpm'
              script {
                  def server = Artifactory.server 'zextras-artifactory'
                  def buildInfo
                  def uploadSpec

                  buildInfo = Artifactory.newBuildInfo()
                  uploadSpec = """{
                      "files": [
                          {
                              "pattern": "artifacts/carbonio-zal*.deb",
                              "target": "ubuntu-playground/pool/",
                              "props": "deb.distribution=bionic;deb.distribution=focal;deb.component=main;deb.architecture=all"
                          },
                          {
                              "pattern": "artifacts/(carbonio-zal)-(*).rpm",
                              "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                              "props": "rpm.metadata.arch=noarch;rpm.metadata.vendor=zextras"
                          }
                      ]
                  }"""
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
              }
          }
      }
      stage('Upload To Devel') {
          when {
            branch 'main'
          }
          steps {
              unstash 'artifacts-deb'
              script {
                  def server = Artifactory.server 'zextras-artifactory'
                  def buildInfo
                  def uploadSpec

                  buildInfo = Artifactory.newBuildInfo()
                  uploadSpec = '''{
                      "files": [
                          {
                              "pattern": "artifacts/*.deb",
                              "target": "ubuntu-devel/pool/",
                              "props": "deb.distribution=focal;deb.component=main;deb.architecture=all"
                          }
                      ]
                  }'''
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
              }
          }
      }
      stage('Upload & Promotion Config') {
          when {
              anyOf {
                  branch 'release/*'
                  buildingTag()
              }
          }
          steps {
              unstash 'artifacts-deb'
              unstash 'artifacts-rpm'
              script {
                  def server = Artifactory.server 'zextras-artifactory'
                  def buildInfo
                  def uploadSpec
                  def config

                  //ubuntu
                  buildInfo = Artifactory.newBuildInfo()
                  buildInfo.name += "-ubuntu"
                  uploadSpec= """{
                      "files": [
                          {
                              "pattern": "artifacts/carbonio-zal*.deb",
                              "target": "ubuntu-rc/pool/",
                              "props": "deb.distribution=bionic;deb.distribution=focal;deb.component=main;deb.architecture=all"
                          }
                      ]
                  }"""
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                  config = [
                          'buildName'          : buildInfo.name,
                          'buildNumber'        : buildInfo.number,
                          'sourceRepo'         : 'ubuntu-rc',
                          'targetRepo'         : 'ubuntu-release',
                          'comment'            : 'Do not change anything! Just press the button',
                          'status'             : 'Released',
                          'includeDependencies': false,
                          'copy'               : true,
                          'failFast'           : true
                  ]
                  Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: "Ubuntu Promotion to Release"
                  server.publishBuildInfo buildInfo

                  //centos8
                  buildInfo = Artifactory.newBuildInfo()
                  buildInfo.name += "-centos8"
                  uploadSpec= """{
                      "files": [
                          {
                              "pattern": "artifacts/(carbonio-zal)-(*).rpm",
                              "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                              "props": "rpm.metadata.arch=noarch;rpm.metadata.vendor=zextras"
                          }
                      ]
                  }"""
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                  config = [
                          'buildName'          : buildInfo.name,
                          'buildNumber'        : buildInfo.number,
                          'sourceRepo'         : 'centos8-rc',
                          'targetRepo'         : 'centos8-release',
                          'comment'            : 'Do not change anything! Just press the button',
                          'status'             : 'Released',
                          'includeDependencies': false,
                          'copy'               : true,
                          'failFast'           : true
                  ]
                  Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: "Centos8 Promotion to Release"
                  server.publishBuildInfo buildInfo

              }
          }
       }
    }
}
