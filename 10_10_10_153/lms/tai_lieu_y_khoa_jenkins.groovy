pipeline {
    agent { label '153_giao_viec' }

    environment  {
        GIT_REPO = "https://github.com/thonguyenduc2010/odoo18.git"
        DB_NAME = "tai_lieu_y_khoa"
        /* TELEGRAM_BOT_TOKEN = "AAHzH1m5fC_e4x1MdVeJl8aF-llVNtbjNpw"
        TELEGRAM_CHAT_ID = "-4064083384" */
    }

    stages {

        stage('Check User') {
            steps {
                sh 'whoami'
                sh 'groups'
                sh 'pwd'
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

                        sudo mkdir -p data pg_data
                        sudo chown -R $USER entrypoint.sh data pg_data etc
                        sudo chmod -R 777 entrypoint.sh
                        sudo chmod -R 777 data pg_data etc

                        docker-compose up -d

                        touch .deployed
                    """
                    sendTelegramMessage("🚀 App tài liệu y khoa đã được triển khai thành công!")
                } else {
                    echo "Updating"
                    sh """
                        docker-compose restart
                    """
                    sendTelegramMessage("♻️ App tài liệu y khoa đã được cập nhật và restart!")
                }
            }
            }
        }
    }

    post {
        success {
            script {
                sendTelegramMessage("✅ Pipeline job tai_lieu_y_khoa đã chạy thành công!")
            }
        }
//         failure {
//             script {
//                 sendTelegramMessage("❌ Pipeline job tai_lieu_y_khoa đã gặp lỗi! Kiểm tra log.")
//             }
//         }
    }
}

def sendTelegramMessage(String message) {
    sh """
        curl -s -X POST "https://api.telegram.org/bot6102275063:${env.TELEGRAM_BOT_TOKEN}/sendMessage" \
        -d chat_id=${env.TELEGRAM_CHAT_ID} \
        -d text="${message}"
    """
}