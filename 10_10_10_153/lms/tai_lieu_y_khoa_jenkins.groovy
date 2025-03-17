pipeline {
    agent { label '153_giao_viec' }

    enviroment {
        APP_PATH = "/data/tai_lieu_y_khoa"
        COMPOSE_FILE = "/data/tai_lieu_y_khoa/docker-compose.yml"
        GIT_REPO = "https://github.com/thonguyenduc2010/odoo18.git"
        DB_NAME = "tai_lieu_y_khoa"
    }

    stages {

        stage('Check deploy'){
            steps {
                script {
                    if (!fileExists("${APP_PATH}/.deployed")) {
                        /* nếu tồn tại file deployed thì tức là đã deploy */
                        env.CHECK_DEPLOY = "false"
                    } else {
                        env.CHECK_DEPLOY = "true"
                    }
                }
            }
        }

        stage('Checkout code') {
            steps {
                git branch: env.Branch_Name,
                credentialsId: env.Git_Credentials_Id,
                url: "${GIT_REPO}"
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

                        mv docker-compose.yml.example docker-compose.yml
                        mv etc/odoo.conf.example etc/odoo.conf

                        mkdir data pg_data
                        chmod -R 777 entrypoint.sh
                        chmod -R 777 data pg_data etc

                        docker-compose up -d

                        curl -X POST \
                        -F "master_pwd=123123" \
                        -F "name=${DB_NAME}" \
                        -F "login=root" \
                        -F "phone=" \
                        -F "password=123123" \
                        -F "lang=vi_VN" \
                        -F "country_code=vn" \
                        http://127.0.0.1:17000/web/database/create

                        sed -i 's@db_name = False@db_name = ${DB_NAME}@g' "${APP_PATH}/etc/odoo.conf"

                        docker-compose restart

                        touch ${APP_PATH}/.deployed
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
                        echo "✅ Deploy thành công!"
                        /* sh """
                            curl -X POST -H 'Content-Type: application/json' \
                            -d '{"status": "success", "message": "Deploy thành công!", "job": "${env.JOB_NAME}", "build": "${env.BUILD_NUMBER}"}' \
                            https://your-webhook-url.com
                        """ */
                    }
                }
                failure {
                    script {
                        echo "❌ Deploy thất bại!"
                        /*sh """
                            curl -X POST -H 'Content-Type: application/json' \
                            -d '{"status": "failure", "message": "Deploy thất bại!", "job": "${env.JOB_NAME}", "build": "${env.BUILD_NUMBER}"}' \
                            https://your-webhook-url.com
                        """ */
                    }
                }
                aborted {
                    script {
                        echo "⚠️ Deploy bị hủy!"
                        /* sh """
                            curl -X POST -H 'Content-Type: application/json' \
                            -d '{"status": "aborted", "message": "Deploy bị hủy!", "job": "${env.JOB_NAME}", "build": "${env.BUILD_NUMBER}"}' \
                            https://your-webhook-url.com
                        """ */
                    }
                }
        }
    }
}
