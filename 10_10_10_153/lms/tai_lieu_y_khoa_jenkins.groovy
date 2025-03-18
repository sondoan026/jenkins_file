pipeline {
    agent { label '153_giao_viec' }

    environment  {
        GIT_REPO = "https://github.com/thonguyenduc2010/odoo18.git"
        DB_NAME = "tai_lieu_y_khoa"
    }

    stages {

        stage('Check User') {
            steps {
                sh 'whoami'
                sh 'groups'
            }
        }

        stage('Check deploy'){
            steps {
                script {
                    env.CHECK_DEPLOY = fileExists(".deployed") ? "true" : "false"
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
                if (env.CHECK_DEPLOY == "false") {
                    echo "First deployment"
                    sh """
                        mv docker-compose.yml.example docker-compose.yml
                        mv etc/odoo.conf.example etc/odoo.conf

                        mkdir -p data pg_data
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
                        http://127.0.0.1:18002/web/database/create

                        sed -i 's@db_name = False@db_name = ${DB_NAME}@g' "/etc/odoo.conf"

                        docker-compose restart

                        touch .deployed
                    """
                } else {
                    echo "Updating"
                    sh """
                        docker-compose restart
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
    }
}
