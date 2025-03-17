pipeline {
    agent { label '153_giao_viec' }

    enviroment = {
        APP_PATH = "/data/tai_lieu_y_khoa"
        COMPOSE_FILE = "/data/tai_lieu_y_khoa/docker-compose.yml"
        GIT_REPO = "https://github.com/thonguyenduc2010/odoo18.git"
    }

    stages {

        stage('Check deploy'){
            steps {
                script {
                    if (!fileExist("${APP_PATH/.deployed}")){
                        env.CHECK_DEPLOY = "true" /* nếu tồn tại file deployed thì tức là đã deploy */
                    } else {
                        env.CHECK_DEPLOY = "false"
                    }
                }
            }
        }

        stage('Checkout code') {
            steps {
                git branch: env.Branch_Name,
                credentialsId: env.Git_Credentials_Id,
                url: ${GIT_REPO}
            }
        }

        stage('Deploy'){
            steps {
                script {
                if (env.CHECK_DEPLOY == "true") {
                    echo "First deployment"
                    sh """
                        mkdir -p ${APP_PATH}
                        rsync -av -delete ${env.WORKSPACE}/ ${APP_PATH}/
                        cd ${APP_PATH}
                        chmod -R 777 addons
                        chmod -R 777 etc
                        mkdir postgresql
                        chmod -R 777 postgresql
                        mkdir ${APP_PATH}/data
                        chmod -R 777 /data/api/data
                        chmod -R 777 entrypoint.sh
                        docker-compose up -d
                    """
                } else {
                    echo "Updating"
                    sh """
                        rsync -av -delete ${env.WORKSPACE}/ ${APP_PATH}/
                        cd ${APP_PATH} && docker-compose restart
                    """
                }
            }
            }
        }

        stage('Clean Up'){
            steps {
                script {
                    sh "docker system prune -f"
                }
            }
        }

        post {
            success {
                script {
                    echo "Deploy success!"
                }
            }

            failure {
                script {
                    echo "Deploy failure!"
                }
            }

            aborted {
                script {
                    echo "Deploy cancel!"
                }
            }
        }
    }
}
